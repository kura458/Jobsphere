package com.jobsphere.jobsite.service.seeker;

import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.dto.seeker.BasicInfoRequest;
import com.jobsphere.jobsite.dto.seeker.BasicInfoResponse;
import com.jobsphere.jobsite.exception.AuthException;
import com.jobsphere.jobsite.exception.ResourceNotFoundException;
import com.jobsphere.jobsite.model.seeker.Seeker;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.repository.SeekerRepository;
import com.jobsphere.jobsite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeekerService {
    private final SeekerRepository seekerRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));
    }

    @Transactional
    public BasicInfoResponse saveOrUpdateBasicInfo(BasicInfoRequest request) {
        User user = getAuthenticatedUser();

        if (user.getUserType() != UserType.SEEKER) {
            throw new AuthException("Only seekers can update basic info");
        }

        UUID userId = user.getId();
        Seeker seeker = seekerRepository.findById(userId).orElse(null);

        if (seeker == null) {
            // CREATE: Fresh Seeker object
            seeker = Seeker.builder()
                    .id(userId)
                    .firstName(request.getFirstName())
                    .middleName(request.getMiddleName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .gender(request.getGender())
                    .dateOfBirth(request.getDateOfBirth())
                    .build();
            seekerRepository.save(seeker);
        } else {
            // UPDATE: Only update fields on the managed entity
            seeker.setFirstName(request.getFirstName());
            seeker.setMiddleName(request.getMiddleName());
            seeker.setLastName(request.getLastName());
            seeker.setPhone(request.getPhone());
            seeker.setGender(request.getGender());
            seeker.setDateOfBirth(request.getDateOfBirth());
            // No need to call .save(seeker), Hibernate will auto-flush
        }

        // Always retrieve the most up-to-date managed entity
        Seeker updatedSeeker = seekerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seeker not found"));

        return mapToResponse(updatedSeeker, user);
    }

    @Transactional(readOnly = true)
    public BasicInfoResponse getBasicInfo() {
        User user = getAuthenticatedUser();

        Seeker seeker = seekerRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Seeker profile not found"));

        return mapToResponse(seeker, user);
    }

    private BasicInfoResponse mapToResponse(Seeker seeker, User user) {
        int completion = calculateCompletion(seeker);

        return BasicInfoResponse.builder()
                .id(seeker.getId())
                .firstName(seeker.getFirstName())
                .middleName(seeker.getMiddleName())
                .lastName(seeker.getLastName())
                .phone(seeker.getPhone())
                .gender(seeker.getGender())
                .dateOfBirth(seeker.getDateOfBirth())
                .email(user.getEmail())
                .profileCompletion(completion + "%")
                .build();
    }

    private int calculateCompletion(Seeker seeker) {
        int total = 6, completed = 0;
        if (seeker.getFirstName() != null && !seeker.getFirstName().trim().isEmpty()) completed++;
        if (seeker.getMiddleName() != null && !seeker.getMiddleName().trim().isEmpty()) completed++;
        if (seeker.getLastName() != null && !seeker.getLastName().trim().isEmpty()) completed++;
        if (seeker.getPhone() != null && !seeker.getPhone().trim().isEmpty()) completed++;
        if (seeker.getGender() != null) completed++;
        if (seeker.getDateOfBirth() != null) completed++;
        return (completed * 100) / total;
    }
}