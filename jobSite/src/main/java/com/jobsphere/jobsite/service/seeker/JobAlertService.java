package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.dto.job.JobAlertRequest;
import com.jobsphere.jobsite.dto.job.JobAlertResponse;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.seeker.JobAlert;
import com.jobsphere.jobsite.model.seeker.Seeker;
import com.jobsphere.jobsite.repository.seeker.JobAlertRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerRepository;
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
    private final AuthenticationService authenticationService;

    @Transactional
    public JobAlertResponse createAlert(JobAlertRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        Seeker seeker = seekerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));

        JobAlert alert = JobAlert.builder()
                .seeker(seeker)
                .keywords(request.keywords())
                .category(request.category())
                .jobType(request.jobType())
                .preferredLocation(request.preferredLocation())
                .isActive(true)
                .build();

        alert = jobAlertRepository.save(alert);
        log.info("Job alert created: {} for seeker: {}", alert.getId(), seeker.getId());

        return mapToResponse(alert);
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
