package com.jobsphere.jobsite.controller.cvtemplate;

import com.jobsphere.jobsite.dto.cvtemplate.CVBuilderRequest;
import com.jobsphere.jobsite.dto.cvtemplate.CVTemplateResponse;
import com.jobsphere.jobsite.service.cvtemplate.CVBuilderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cv-builder")
@RequiredArgsConstructor
@Tag(name = "CV Builder", description = "Seeker API for building CVs from templates")
public class CVBuilderController {
    private final CVBuilderService cvBuilderService;

    @GetMapping("/templates")
    @Operation(summary = "Get all active CV templates")
    public ResponseEntity<List<CVTemplateResponse>> getActiveTemplates() {
        List<CVTemplateResponse> templates = cvBuilderService.getActiveTemplates();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/builder/{templateId}")
    @Operation(summary = "Get template with auto-filled data from profile")
    public ResponseEntity<Map<String, Object>> getTemplateWithAutoFill(@PathVariable UUID templateId) {
        Map<String, Object> result = cvBuilderService.getTemplateWithAutoFill(templateId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/preview")
    @Operation(summary = "Preview generated CV")
    public ResponseEntity<Map<String, Object>> previewCV(@Valid @RequestBody CVBuilderRequest request) {
        Map<String, Object> preview = cvBuilderService.previewCV(request);
        return ResponseEntity.ok(preview);
    }

    @PostMapping("/download")
    @Operation(summary = "Download generated CV as PDF")
    public ResponseEntity<org.springframework.core.io.Resource> downloadCV(
            @Valid @RequestBody CVBuilderRequest request) {
        byte[] pdfBytes = cvBuilderService.prepareDownload(request);
        org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(
                pdfBytes);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resume.pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
    }
}
