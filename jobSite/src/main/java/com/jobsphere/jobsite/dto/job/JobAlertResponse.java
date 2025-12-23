package com.jobsphere.jobsite.dto.job;

import java.time.Instant;
import java.util.UUID;

public record JobAlertResponse(
        UUID id,
        UUID seekerId,
        String keywords,
        String category,
        String jobType,
        String preferredLocation,
        Boolean isActive,
        Instant createdAt) {
}
