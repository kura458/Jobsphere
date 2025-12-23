package com.jobsphere.jobsite.service.job;

import com.jobsphere.jobsite.dto.job.JobResponse;
import com.jobsphere.jobsite.dto.seeker.FullProfileResponse;
import com.jobsphere.jobsite.model.job.Job;
import com.jobsphere.jobsite.model.seeker.JobAlert;
import com.jobsphere.jobsite.model.seeker.SeekerSector;
import com.jobsphere.jobsite.model.seeker.SeekerSkill;
import com.jobsphere.jobsite.repository.job.JobRepository;
import com.jobsphere.jobsite.repository.seeker.*;
import com.jobsphere.jobsite.service.seeker.SeekerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobMatchingService {
    private final JobRepository jobRepository;
    private final JobAlertRepository jobAlertRepository;
    private final SeekerService seekerService;
    private final JobService jobService;
    private final SeekerSkillRepository seekerSkillRepository;
    private final SeekerSectorRepository seekerSectorRepository;

    @Transactional(readOnly = true)
    public List<JobResponse> getMatchedJobsForSeeker(UUID userId) {
        List<JobAlert> alerts = jobAlertRepository.findBySeekerId(userId);

        // Fetch seeker sectors and skills for profile-based matching
        List<String> seekerSectors = seekerSectorRepository.findBySeekerId(userId).stream()
                .map(SeekerSector::getSector)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        List<String> seekerSkills = seekerSkillRepository.findBySeekerId(userId).stream()
                .map(SeekerSkill::getSkill)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        List<Job> activeJobs = jobRepository.findByIsActiveTrue().stream()
                .filter(j -> "OPEN".equals(j.getStatus()))
                .collect(Collectors.toList());

        Set<Job> matchedJobs = new HashSet<>();

        // 1. Matches from Alerts
        for (JobAlert alert : alerts) {
            if (!alert.getIsActive())
                continue;

            for (Job job : activeJobs) {
                if (isMatch(job, alert)) {
                    matchedJobs.add(job);
                }
            }
        }

        // 2. Matches from Profile (as supplementary)
        for (Job job : activeJobs) {
            if (matchedJobs.contains(job))
                continue;

            // Match by sector (fuzzy)
            if (job.getCategory() != null) {
                boolean sectorMatch = false;
                for (String sector : seekerSectors) {
                    if (isCategoryRelated(sector, job.getCategory())) {
                        sectorMatch = true;
                        break;
                    }
                }
                if (sectorMatch) {
                    matchedJobs.add(job);
                    continue;
                }
            }

            // Match by skills (if skill keyword is in title or description)
            for (String skill : seekerSkills) {
                if (skill.length() > 3 && ((job.getTitle() != null && job.getTitle().toLowerCase().contains(skill)) ||
                        (job.getDescription() != null && job.getDescription().toLowerCase().contains(skill)))) {
                    matchedJobs.add(job);
                    break;
                }
            }
        }

        return matchedJobs.stream()
                .map(jobService::mapToResponse)
                .sorted(Comparator.comparing(JobResponse::createdAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FullProfileResponse> getRecommendedCandidates(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        String jobCategory = job.getCategory();
        String jobTitle = job.getTitle() != null ? job.getTitle().toLowerCase() : "";
        String jobDesc = job.getDescription() != null ? job.getDescription().toLowerCase() : "";

        // Extract tags/keywords from job
        Set<String> jobKeywords = new HashSet<>();
        if (job.getTitle() != null)
            jobKeywords.addAll(Arrays.asList(job.getTitle().toLowerCase().split("\\s+")));
        if (job.getExperienceDescription() != null) {
            jobKeywords.addAll(Arrays.asList(job.getExperienceDescription().toLowerCase().split(",\\s*")));
        }

        // Fetch candidates matching sector
        List<UUID> candidateIds = new ArrayList<>();

        seekerSectorRepository.findAll().stream()
                .filter(s -> isCategoryRelated(s.getSector(), jobCategory))
                .map(s -> s.getSeeker().getId())
                .forEach(candidateIds::add);

        // Fetch candidates matching skills
        seekerSkillRepository.findAll().stream()
                .filter(s -> {
                    String skill = s.getSkill() != null ? s.getSkill().toLowerCase() : "";
                    if (skill.length() < 3)
                        return false;

                    // Skill in job title or description
                    if (jobTitle.contains(skill) || jobDesc.contains(skill))
                        return true;

                    // Job keywords in skill
                    for (String kw : jobKeywords) {
                        if (kw.length() > 3 && skill.contains(kw))
                            return true;
                    }
                    return false;
                })
                .map(s -> s.getSeeker().getId())
                .forEach(id -> {
                    if (!candidateIds.contains(id))
                        candidateIds.add(id);
                });

        return candidateIds.stream()
                .distinct()
                .limit(15)
                .map(seekerService::getFullProfileById)
                .collect(Collectors.toList());
    }

    private boolean isMatch(Job job, JobAlert alert) {
        // 1. Category match (more lenient)
        if (alert.getCategory() != null && !alert.getCategory().isEmpty()) {
            String alertCat = alert.getCategory();
            String jobCat = job.getCategory();

            if (jobCat != null) {
                boolean match = alertCat.equalsIgnoreCase(jobCat) ||
                        isCategoryRelated(alertCat, jobCat);
                if (!match)
                    return false;
            }
        }

        // 2. Job Type match
        if (alert.getJobType() != null && !alert.getJobType().isEmpty()) {
            if (!alert.getJobType().equalsIgnoreCase(job.getJobType()))
                return false;
        }

        // 3. Location match
        if (alert.getPreferredLocation() != null && !alert.getPreferredLocation().isEmpty()) {
            String jobCity = job.getAddress() != null ? job.getAddress().getCity() : "";
            String jobRegion = job.getAddress() != null ? job.getAddress().getRegion() : "";
            boolean cityMatch = jobCity != null
                    && jobCity.toLowerCase().contains(alert.getPreferredLocation().toLowerCase());
            boolean regionMatch = jobRegion != null
                    && jobRegion.toLowerCase().contains(alert.getPreferredLocation().toLowerCase());
            if (!cityMatch && !regionMatch)
                return false;
        }

        // 4. Keyword match
        if (alert.getKeywords() != null && !alert.getKeywords().isEmpty()) {
            String[] keywords = alert.getKeywords().split(",");
            boolean keywordMatched = false;
            for (String kw : keywords) {
                String cleanKw = kw.trim().toLowerCase();
                if (cleanKw.isEmpty())
                    continue;
                if ((job.getTitle() != null && job.getTitle().toLowerCase().contains(cleanKw)) ||
                        (job.getDescription() != null && job.getDescription().toLowerCase().contains(cleanKw))) {
                    keywordMatched = true;
                    break;
                }
            }
            if (!keywordMatched)
                return false;
        }

        return true;
    }

    private boolean isCategoryRelated(String alertCat, String jobCat) {
        String a = alertCat.toLowerCase();
        String j = jobCat.toLowerCase();

        // Technology Mapping
        if ((a.contains("tech") || a.contains("it")) && j.equals("technology"))
            return true;
        // Marketing/Sales Mapping
        if ((a.contains("marketing") || a.contains("sales")) && j.equals("marketing"))
            return true;
        // Design Mapping
        if (a.contains("design") && j.equals("design"))
            return true;
        // Healthcare Mapping
        if (a.contains("health") && j.equals("healthcare"))
            return true;
        // Finance Mapping
        if ((a.contains("finance") || a.contains("banking")) && j.equals("finance"))
            return true;
        // Education Mapping
        if (a.contains("education") && j.equals("education"))
            return true;
        // Engineering Mapping
        if (a.contains("engineering") && j.equals("engineering"))
            return true;

        return a.contains(j) || j.contains(a);
    }
}
