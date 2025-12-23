package com.jobsphere.jobsite.service.job;

import com.jobsphere.jobsite.dto.job.JobResponse;
import com.jobsphere.jobsite.dto.seeker.FullProfileResponse;
import com.jobsphere.jobsite.model.job.Job;
import com.jobsphere.jobsite.model.seeker.JobAlert;
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
        if (alerts.isEmpty())
            return Collections.emptyList();

        List<Job> activeJobs = jobRepository.findAll().stream()
                .filter(j -> j.getIsActive() && "OPEN".equals(j.getStatus()))
                .collect(Collectors.toList());

        Set<Job> matchedJobs = new HashSet<>();
        for (JobAlert alert : alerts) {
            if (!alert.getIsActive())
                continue;

            for (Job job : activeJobs) {
                if (isMatch(job, alert)) {
                    matchedJobs.add(job);
                }
            }
        }

        return matchedJobs.stream()
                .map(jobService::mapToResponse)
                .sorted(Comparator.comparing(JobResponse::createdAt).reversed())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FullProfileResponse> getRecommendedCandidates(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // 1. Filter seekers by category (SeekerSector)
        List<UUID> seekerIdsBySector = seekerSectorRepository.findAll().stream()
                .filter(s -> s.getSector().equalsIgnoreCase(job.getCategory()))
                .map(s -> s.getSeeker().getId())
                .collect(Collectors.toList());

        // 2. Filter seekers by keywords in skills
        String title = job.getTitle();
        String[] keywords = title != null ? title.split(" ") : new String[0];

        List<UUID> seekerIdsBySkill = seekerSkillRepository.findAll().stream()
                .filter(s -> {
                    for (String kw : keywords) {
                        if (kw.length() > 3 && s.getSkill().toLowerCase().contains(kw.toLowerCase()))
                            return true;
                    }
                    return false;
                })
                .map(s -> s.getSeeker().getId())
                .collect(Collectors.toList());

        Set<UUID> candidateIds = new HashSet<>(seekerIdsBySector);
        candidateIds.addAll(seekerIdsBySkill);

        return candidateIds.stream()
                .limit(10)
                .map(seekerService::getFullProfileById)
                .collect(Collectors.toList());
    }

    private boolean isMatch(Job job, JobAlert alert) {
        // Category match
        if (alert.getCategory() != null && !alert.getCategory().isEmpty()) {
            if (!alert.getCategory().equalsIgnoreCase(job.getCategory()))
                return false;
        }

        // Job Type match
        if (alert.getJobType() != null && !alert.getJobType().isEmpty()) {
            if (!alert.getJobType().equalsIgnoreCase(job.getJobType()))
                return false;
        }

        // Location match
        if (alert.getPreferredLocation() != null && !alert.getPreferredLocation().isEmpty()) {
            String jobCity = job.getAddress() != null ? job.getAddress().getCity() : "";
            String jobRegion = job.getAddress() != null ? job.getAddress().getRegion() : "";
            boolean cityMatch = jobCity != null && jobCity.equalsIgnoreCase(alert.getPreferredLocation());
            boolean regionMatch = jobRegion != null && jobRegion.equalsIgnoreCase(alert.getPreferredLocation());
            if (!cityMatch && !regionMatch)
                return false;
        }

        // Keyword match
        if (alert.getKeywords() != null && !alert.getKeywords().isEmpty()) {
            String[] keywords = alert.getKeywords().split(",");
            boolean keywordMatched = false;
            for (String kw : keywords) {
                String cleanKw = kw.trim().toLowerCase();
                if (cleanKw.isEmpty())
                    continue;
                if (job.getTitle().toLowerCase().contains(cleanKw) ||
                        job.getDescription().toLowerCase().contains(cleanKw)) {
                    keywordMatched = true;
                    break;
                }
            }
            if (!keywordMatched)
                return false;
        }

        return true;
    }
}
