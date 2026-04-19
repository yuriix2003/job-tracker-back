package com.yashfi.job.job_apps.controller;

import com.yashfi.job.job_apps.dto.JobRequest;
import com.yashfi.job.job_apps.dto.JobResponse;
import com.yashfi.job.job_apps.service.JobService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobService jobService;

    // Public endpoint - Get all jobs with optional filters
    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location) {
        List<JobResponse> jobs = jobService.getAllJobs(keyword, location);
        return ResponseEntity.ok(jobs);
    }

    // Public endpoint - Get single job by ID
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        JobResponse job = jobService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    // Company only - Create new job
    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        JobResponse job = jobService.createJob(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }

    // Company only - Update job
    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        JobResponse job = jobService.updateJob(id, request, userEmail);
        return ResponseEntity.ok(job);
    }

    // Company only - Delete job
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        jobService.deleteJob(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    // Company only - Get my jobs
    @GetMapping("/company/my-jobs")
    public ResponseEntity<List<JobResponse>> getMyJobs(Authentication authentication) {
        String userEmail = authentication.getName();
        List<JobResponse> jobs = jobService.getMyJobs(userEmail);
        return ResponseEntity.ok(jobs);
    }
}