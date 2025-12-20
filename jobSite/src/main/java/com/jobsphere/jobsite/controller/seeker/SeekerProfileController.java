package com.jobsphere.jobsite.controller.seeker;

import com.jobsphere.jobsite.dto.seeker.BasicInfoRequest;
import com.jobsphere.jobsite.dto.seeker.BasicInfoResponse;
import com.jobsphere.jobsite.service.seeker.SeekerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seekers/profile")
@RequiredArgsConstructor
public class SeekerProfileController {
    private final SeekerService seekerService;

    @PostMapping("/basic-info")
    public ResponseEntity<BasicInfoResponse> saveBasicInfo(@Valid @RequestBody BasicInfoRequest request) {
        return ResponseEntity.ok(seekerService.saveOrUpdateBasicInfo(request));
    }

    @GetMapping("/basic-info")
    public ResponseEntity<BasicInfoResponse> getBasicInfo() {
        return ResponseEntity.ok(seekerService.getBasicInfo());
    }
}