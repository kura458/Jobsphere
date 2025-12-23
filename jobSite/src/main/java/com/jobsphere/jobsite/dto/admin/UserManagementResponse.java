package com.jobsphere.jobsite.dto.admin;

import com.jobsphere.jobsite.constant.UserType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserManagementResponse {
    private UUID id;
    private String email;
    private UserType userType;
    private boolean isActive;
    private boolean emailVerified;
    private Instant lastLogin;
    private Instant createdAt;

    // Profile info
    private String name; // firstName + lastName for Seekers, companyName for Employers
    private String profileImageUrl; // profileImageUrl for Seekers, logoUrl for Employers
}
