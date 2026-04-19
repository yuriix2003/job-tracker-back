package com.yashfi.job.job_apps.controller;

import com.yashfi.job.job_apps.model.ApplicantProfile;
import com.yashfi.job.job_apps.model.CompanyProfile;
import com.yashfi.job.job_apps.model.User;
import com.yashfi.job.job_apps.repository.ApplicantProfileRepository;
import com.yashfi.job.job_apps.repository.CompanyProfileRepository;
import com.yashfi.job.job_apps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicantProfileRepository applicantProfileRepository;

    @Autowired
    private CompanyProfileRepository companyProfileRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userService.getCurrentUser(userEmail);

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);

        if (user.getRole() == User.UserRole.APPLICANT) {
            ApplicantProfile profile = applicantProfileRepository.findByUserId(user.getId())
                    .orElse(null);
            response.put("profile", profile);
        } else if (user.getRole() == User.UserRole.COMPANY) {
            CompanyProfile profile = companyProfileRepository.findByUserId(user.getId())
                    .orElse(null);
            response.put("profile", profile);
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userService.getCurrentUser(userEmail);

        if (user.getRole() == User.UserRole.APPLICANT) {
            ApplicantProfile profile = applicantProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Profile not found"));

            if (updates.containsKey("phone")) {
                profile.setPhone((String) updates.get("phone"));
            }
            if (updates.containsKey("location")) {
                profile.setLocation((String) updates.get("location"));
            }
            if (updates.containsKey("resumePath")) {
                profile.setResumePath((String) updates.get("resumePath"));
            }

            applicantProfileRepository.save(profile);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("profile", profile);
            return ResponseEntity.ok(response);

        } else if (user.getRole() == User.UserRole.COMPANY) {
            CompanyProfile profile = companyProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Profile not found"));

            if (updates.containsKey("companyName")) {
                profile.setCompanyName((String) updates.get("companyName"));
            }
            if (updates.containsKey("description")) {
                profile.setDescription((String) updates.get("description"));
            }
            if (updates.containsKey("website")) {
                profile.setWebsite((String) updates.get("website"));
            }

            companyProfileRepository.save(profile);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("profile", profile);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().build();
    }
}