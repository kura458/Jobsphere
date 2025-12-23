package com.jobsphere.jobsite.service.admin;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.admin.UserManagementResponse;
import com.jobsphere.jobsite.dto.admin.UserUpdateRequest;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.employer.CompanyProfile;
import com.jobsphere.jobsite.model.seeker.Seeker;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.employer.CompanyProfileRepository;
import com.jobsphere.jobsite.repository.seeker.SeekerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {
    private final UserRepository userRepository;
    private final SeekerRepository seekerRepository;
    private final CompanyProfileRepository companyProfileRepository;

    @Transactional(readOnly = true)
    public List<UserManagementResponse> getAllUsers(UserType type) {
        List<User> users;
        if (type != null) {
            users = userRepository.findByUserType(type);
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserManagementResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    @Transactional
    public UserManagementResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(request.isActive());
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }

        userRepository.save(user);
        log.info("Admin updated user {}: active={}, verified={}", id, user.isActive(), user.isEmailVerified());

        return mapToResponse(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setDeletedAt(Instant.now());
        user.setActive(false);
        userRepository.save(user);

        log.info("Admin soft-deleted user {}", id);
    }

    private UserManagementResponse mapToResponse(User user) {
        UserManagementResponse.UserManagementResponseBuilder builder = UserManagementResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userType(user.getUserType())
                .isActive(user.isActive())
                .emailVerified(user.isEmailVerified())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt());

        if (user.getUserType() == UserType.SEEKER) {
            Optional<Seeker> seeker = seekerRepository.findById(user.getId());
            seeker.ifPresent(s -> {
                builder.name(s.getFirstName() + " " + (s.getLastName() != null ? s.getLastName() : ""));
                builder.profileImageUrl(s.getProfileImageUrl());
            });
        } else if (user.getUserType() == UserType.EMPLOYER) {
            Optional<CompanyProfile> company = companyProfileRepository.findByUserId(user.getId());
            company.ifPresent(c -> {
                builder.name(c.getCompanyName());
                builder.profileImageUrl(c.getLogoUrl());
            });
        }

        return builder.build();
    }
}
