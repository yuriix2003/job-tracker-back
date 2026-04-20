package com.yashfi.job.job_apps.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    private Storage storage;
    private boolean firebaseEnabled = false;

    @PostConstruct
    public void initialize() {
        try {
            GoogleCredentials credentials;

            // Check for base64 encoded credentials (production)
            String base64Creds = System.getenv("FIREBASE_CREDENTIALS_BASE64");
            if (base64Creds != null && !base64Creds.isEmpty()) {
                System.out.println("🔍 Using base64 Firebase credentials");
                byte[] decodedBytes = Base64.getDecoder().decode(base64Creds);
                credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decodedBytes));
            } else {
                System.out.println("⚠️  No Firebase credentials found. File upload disabled.");
                firebaseEnabled = false;
                return;
            }

            storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();

            firebaseEnabled = true;
            System.out.println("✅ Firebase Storage initialized successfully");
            System.out.println("📦 Bucket: " + bucketName);

        } catch (Exception e) {
            System.err.println("❌ Firebase initialization failed: " + e.getMessage());
            e.printStackTrace();
            firebaseEnabled = false;
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (!firebaseEnabled) {
            throw new RuntimeException("Firebase Storage is not configured");
        }

        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getBytes());

            // Return public URL
            String downloadUrl = String.format(
                    "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucketName,
                    fileName.replace("/", "%2F")
            );

            System.out.println("✅ File uploaded: " + downloadUrl);
            return downloadUrl;

        } catch (Exception e) {
            System.err.println("❌ File upload failed: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload file: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        if (!firebaseEnabled) {
            return;
        }

        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            BlobId blobId = BlobId.of(bucketName, fileName);
            storage.delete(blobId);
            System.out.println("✅ File deleted: " + fileName);
        } catch (Exception e) {
            System.err.println("⚠️  File deletion failed: " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return firebaseEnabled;
    }

    private String extractFileNameFromUrl(String url) {
        String[] parts = url.split("/o/");
        if (parts.length > 1) {
            String fileName = parts[1].split("\\?")[0];
            return fileName.replace("%2F", "/");
        }
        return "";
    }
}