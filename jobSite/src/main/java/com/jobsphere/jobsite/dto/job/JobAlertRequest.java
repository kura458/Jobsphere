package com.jobsphere.jobsite.dto.job;

import java.util.UUID;

public record JobAlertRequest(
        String keywords,
        String category,
        String jobType,
        String preferredLocation) {
}
