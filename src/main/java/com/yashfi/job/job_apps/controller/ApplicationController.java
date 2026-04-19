package com.yashfi.job.job_apps.controller;

import com.yashfi.job.job_apps.dto.ApplicationRequest;
import com.yashfi.job.job_apps.dto.ApplicationResponse;
import com.yashfi.job.job_apps.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    // Applicant - Apply to a job
    @PostMapping
    public ResponseEntity<ApplicationResponse> applyToJob(
            @RequestParam Long jobId,
            @RequestBody ApplicationRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        ApplicationResponse application = applicationService.applyToJob(jobId, request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(application);
    }

    // Applicant - Get my applications
    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(Authentication authentication) {
        String userEmail = authentication.getName();
        List<ApplicationResponse> applications = applicationService.getMyApplications(userEmail);
        return ResponseEntity.ok(applications);
    }

    // Get single application by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        ApplicationResponse application = applicationService.getApplicationById(id, userEmail);
        return ResponseEntity.ok(application);
    }

    // Company - Get all applications for a specific job
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsForJob(
            @PathVariable Long jobId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        List<ApplicationResponse> applications = applicationService.getApplicationsForJob(jobId, userEmail);
        return ResponseEntity.ok(applications);
    }

    // Company - Update application status
    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate,
            Authentication authentication) {
        String userEmail = authentication.getName();
        String status = statusUpdate.get("status");
        ApplicationResponse application = applicationService.updateApplicationStatus(id, status, userEmail);
        return ResponseEntity.ok(application);
    }
}