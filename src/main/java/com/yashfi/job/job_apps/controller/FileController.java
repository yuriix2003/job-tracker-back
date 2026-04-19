package com.yashfi.job.job_apps.controller;

import com.yashfi.job.job_apps.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {

        if (!fileStorageService.isEnabled()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "File upload is currently disabled. Firebase Storage not configured.");
            error.put("help", "For testing, you can use a dummy URL like: https://example.com/resume.pdf");
            return ResponseEntity.status(503).body(error);
        }

        try {
            String fileUrl = fileStorageService.uploadFile(file);

            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("message", "File uploaded successfully");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "File upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}