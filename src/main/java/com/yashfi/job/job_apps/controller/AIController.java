package com.yashfi.job.job_apps.controller;

import com.yashfi.job.job_apps.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * Analyze resume from uploaded PDF
     */
    @PostMapping("/analyze-resume")
    public ResponseEntity<Map<String, Object>> analyzeResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Map<String, Object> analysis = aiService.analyzeResume(file, userEmail);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to analyze resume: " + e.getMessage()));
        }
    }

    /**
     * Calculate job match score for an application
     */
    @PostMapping("/match-job")
    public ResponseEntity<Map<String, Object>> matchJob(
            @RequestParam Long applicationId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Map<String, Object> match = aiService.calculateJobMatch(applicationId, userEmail);
            return ResponseEntity.ok(match);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to calculate match: " + e.getMessage()));
        }
    }

    /**
     * Interview practice chatbot
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            String message = (String) request.get("message");
            Long jobId = request.get("jobId") != null ?
                    Long.parseLong(request.get("jobId").toString()) : null;

            String userEmail = authentication.getName();
            Map<String, Object> response = aiService.chat(message, jobId, userEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Chat failed: " + e.getMessage()));
        }
    }

    /**
     * Get chat conversation history
     */
    @GetMapping("/chat/history")
    public ResponseEntity<List<Map<String, Object>>> getChatHistory(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Map<String, Object>> history = aiService.getChatHistory(userEmail);
        return ResponseEntity.ok(history);
    }

    /**
     * Get AI analysis for an application
     */
    @GetMapping("/analysis/{applicationId}")
    public ResponseEntity<Map<String, Object>> getAnalysis(
            @PathVariable Long applicationId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Map<String, Object> analysis = aiService.getAnalysis(applicationId, userEmail);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get analysis: " + e.getMessage()));
        }
    }
}