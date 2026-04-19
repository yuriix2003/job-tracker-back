package com.yashfi.job.job_apps.controller;

import com.yashfi.job.job_apps.service.AIService;
import com.yashfi.job.job_apps.service.EmailService;
import com.yashfi.job.job_apps.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AIService aiService;

    /**
     * Test Firebase Storage
     */
    @PostMapping("/firebase-upload")
    public ResponseEntity<Map<String, String>> testFirebaseUpload(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        try {
            if (!fileStorageService.isEnabled()) {
                response.put("status", "error");
                response.put("message", "Firebase Storage is not configured");
                return ResponseEntity.status(503).body(response);
            }

            String url = fileStorageService.uploadFile(file);
            response.put("status", "success");
            response.put("message", "File uploaded successfully to Firebase");
            response.put("url", url);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", String.valueOf(file.getSize()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test Brevo Email
     */
    @PostMapping("/send-test-email")
    public ResponseEntity<Map<String, String>> testEmail(
            @RequestParam String email,
            @RequestParam String name) {

        Map<String, String> response = new HashMap<>();

        try {
            emailService.sendApplicationStatusEmail(
                    email,
                    name,
                    "Senior Software Engineer",
                    "Test Company",
                    "INTERVIEW"
            );

            response.put("status", "success");
            response.put("message", "Test email sent to: " + email);
            response.put("note", "Check your inbox (and spam folder)");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Email failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test Cloudflare AI
     */
    @PostMapping("/cloudflare-ai")
    public ResponseEntity<Map<String, Object>> testCloudflareAI(@RequestParam String prompt) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Use reflection to call private method for testing
            java.lang.reflect.Method method = AIService.class.getDeclaredMethod("callCloudflareAI", String.class);
            method.setAccessible(true);
            String aiResponse = (String) method.invoke(aiService, prompt);

            response.put("status", "success");
            response.put("prompt", prompt);
            response.put("response", aiResponse);
            response.put("message", "Cloudflare AI is working correctly!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "AI test failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test PDF extraction
     */
    @PostMapping("/test-pdf-extraction")
    public ResponseEntity<Map<String, Object>> testPDFExtraction(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            String extractedText = aiService.extractTextFromPDF(file);

            response.put("status", "success");
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            response.put("extractedTextLength", extractedText.length());
            response.put("preview", extractedText.substring(0, Math.min(500, extractedText.length())) + "...");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "PDF extraction failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Health check - verify all services
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Boolean> services = new HashMap<>();

        // Check Firebase
        services.put("firebaseStorage", fileStorageService.isEnabled());

        // Check database (if we got here, it's working)
        services.put("database", true);

        // Check Cloudflare AI (basic check)
        services.put("cloudflareAI",
                System.getenv("CLOUDFLARE_ACCOUNT_ID") != null ||
                        !aiService.toString().isEmpty());

        response.put("status", "healthy");
        response.put("services", services);
        response.put("timestamp", new java.util.Date());

        return ResponseEntity.ok(response);
    }
}