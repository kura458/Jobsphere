package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.dto.job.JobAlertRequest;
import com.jobsphere.jobsite.dto.job.JobAlertResponse;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.seeker.JobAlert;
import com.jobsphere.jobsite.model.seeker.Seeker;
import com.jobsphere.jobsite.model.seeker.SeekerSector;
import com.jobsphere.jobsite.model.seeker.SeekerSkill;
import com.jobsphere.jobsite.repository.seeker.JobAlertRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerSectorRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerSkillRepository;
import com.jobsphere.jobsite.service.shared.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobAlertService {
    private final JobAlertRepository jobAlertRepository;
    private final SeekerRepository seekerRepository;
    private final SeekerSectorRepository seekerSectorRepository;
    private final SeekerSkillRepository seekerSkillRepository;
    private final AuthenticationService authenticationService;

    @Transactional
    public JobAlertResponse createAlert(JobAlertRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        Seeker seeker = seekerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));

        String keywords = request.keywords();
        if (keywords == null || keywords.trim().isEmpty()) {
            keywords = "General"; // Default fallback to prevent 500
        }

        JobAlert alert = JobAlert.builder()
                .seeker(seeker)
                .keywords(keywords)
                .category(request.category())
                .jobType(request.jobType())
                .preferredLocation(request.preferredLocation())
                .isActive(true)
                .build();

        alert = jobAlertRepository.save(alert);
        log.info("Job alert created: {} for seeker: {}", alert.getId(), seeker.getId());

        return mapToResponse(alert);
    }

    @Transactional
    public void syncAlertsWithProfile() {
        UUID userId = authenticationService.getCurrentUserId();
        Seeker seeker = seekerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));

        List<String> sectors = seekerSectorRepository.findBySeekerId(userId).stream()
                .map(SeekerSector::getSector)
                .collect(Collectors.toList());

        List<String> skills = seekerSkillRepository.findBySeekerId(userId).stream()
                .map(SeekerSkill::getSkill)
                .collect(Collectors.toList());

        if (sectors.isEmpty() && skills.isEmpty())
            return;

        List<JobAlert> existing = jobAlertRepository.findBySeekerId(userId);

        // Strategy: If no alerts exist, create one comprehensive alert
        if (existing.isEmpty()) {
            String keywords = skills.isEmpty() ? (sectors.isEmpty() ? "General" : sectors.get(0))
                    : String.join(", ", skills);
            if (keywords.length() > 255)
                keywords = keywords.substring(0, 252) + "...";

            JobAlert alert = JobAlert.builder()
                    .seeker(seeker)
                    .keywords(keywords)
                    .category(sectors.isEmpty() ? null : sectors.get(0))
                    .isActive(true)
                    .build();
            jobAlertRepository.save(alert);
            log.info("Auto-generated job alert based on profile for seeker: {}", userId);
        }
    }

    @Transactional(readOnly = true)
    public List<JobAlertResponse> getMyAlerts() {
        UUID userId = authenticationService.getCurrentUserId();
        return jobAlertRepository.findBySeekerId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAlert(UUID alertId) {
        UUID userId = authenticationService.getCurrentUserId();
        JobAlert alert = jobAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Job alert not found"));

        if (!alert.getSeeker().getId().equals(userId)) {
            throw new IllegalStateException("You can only delete your own alerts");
        }

        jobAlertRepository.delete(alert);
        log.info("Job alert deleted: {}", alertId);
    }

    @Transactional
    public JobAlertResponse toggleAlert(UUID alertId) {
        UUID userId = authenticationService.getCurrentUserId();
        JobAlert alert = jobAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Job alert not found"));

        if (!alert.getSeeker().getId().equals(userId)) {
            throw new IllegalStateException("You can only toggle your own alerts");
        }

        alert.setIsActive(!alert.getIsActive());
        alert = jobAlertRepository.save(alert);
        log.info("Job alert toggle: {} status is now {}", alertId, alert.getIsActive());

        return mapToResponse(alert);
    }

    private JobAlertResponse mapToResponse(JobAlert alert) {
        return new JobAlertResponse(
                alert.getId(),
                alert.getSeeker().getId(),
                alert.getKeywords(),
                alert.getCategory(),
                alert.getJobType(),
                alert.getPreferredLocation(),
                alert.getIsActive(),
                alert.getCreatedAt());
    }
}
