package com.yashfi.job.job_apps.controller;

import com.yashfi.job.job_apps.dto.StatsResponse;
import com.yashfi.job.job_apps.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatsController {

    @Autowired
    private StatsService statsService;

    // Applicant - Get dashboard stats
    @GetMapping("/applicant")
    public ResponseEntity<StatsResponse> getApplicantStats(Authentication authentication) {
        String userEmail = authentication.getName();
        StatsResponse stats = statsService.getApplicantStats(userEmail);
        return ResponseEntity.ok(stats);
    }

    // Company - Get dashboard stats
    @GetMapping("/company")
    public ResponseEntity<StatsResponse> getCompanyStats(Authentication authentication) {
        String userEmail = authentication.getName();
        StatsResponse stats = statsService.getCompanyStats(userEmail);
        return ResponseEntity.ok(stats);
    }
}