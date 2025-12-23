package com.jobsphere.jobsite.dto.admin;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private boolean isActive;
    // We can add more fields if needed, like email verification status
    private Boolean emailVerified;
}
