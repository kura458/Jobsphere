package com.jobsphere.jobsite.controller.job;

import com.jobsphere.jobsite.dto.job.JobAlertRequest;
import com.jobsphere.jobsite.dto.job.JobAlertResponse;
import com.jobsphere.jobsite.dto.job.JobResponse;
import com.jobsphere.jobsite.service.job.JobMatchingService;
import com.jobsphere.jobsite.service.seeker.JobAlertService;
import com.jobsphere.jobsite.service.shared.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/job-alerts")
@RequiredArgsConstructor
@Tag(name = "Job Alerts", description = "API for managing job alerts and matching")
public class JobAlertController {
    private final JobAlertService jobAlertService;
    private final JobMatchingService jobMatchingService;
    private final AuthenticationService authenticationService;

    @PostMapping
    @Operation(summary = "Create a new job alert")
    public ResponseEntity<JobAlertResponse> createAlert(@RequestBody JobAlertRequest request) {
        return ResponseEntity.ok(jobAlertService.createAlert(request));
    }

    @GetMapping
    @Operation(summary = "Get my job alerts")
    public ResponseEntity<List<JobAlertResponse>> getMyAlerts() {
        return ResponseEntity.ok(jobAlertService.getMyAlerts());
    }

    @DeleteMapping("/{alertId}")
    @Operation(summary = "Delete a job alert")
    public ResponseEntity<Void> deleteAlert(@PathVariable UUID alertId) {
        jobAlertService.deleteAlert(alertId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{alertId}/toggle")
    @Operation(summary = "Toggle job alert active status")
    public ResponseEntity<JobAlertResponse> toggleAlert(@PathVariable UUID alertId) {
        return ResponseEntity.ok(jobAlertService.toggleAlert(alertId));
    }

    @GetMapping("/matched")
    @Operation(summary = "Get jobs matching my alerts")
    public ResponseEntity<List<JobResponse>> getMatchedJobs() {
        UUID userId = authenticationService.getCurrentUserId();
        return ResponseEntity.ok(jobMatchingService.getMatchedJobsForSeeker(userId));
    }
}
