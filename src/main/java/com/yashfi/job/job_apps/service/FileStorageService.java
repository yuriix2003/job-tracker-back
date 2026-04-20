package com.yashfi.job.job_apps.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    private Storage storage;
    private boolean firebaseEnabled = false;

    @PostConstruct
    public void initialize() {
        System.out.println("🔍 Initializing Firebase Storage...");
        System.out.println("📦 Bucket: " + bucketName);

        try {
            GoogleCredentials credentials;

            // Try base64 credentials first (production)
            String base64Creds = System.getenv("FIREBASE_CREDENTIALS_BASE64");
            if (base64Creds != null && !base64Creds.isEmpty()) {
                System.out.println("✅ Using base64 Firebase credentials from environment");
                try {
                    byte[] decodedBytes = Base64.getDecoder().decode(base64Creds);
                    credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decodedBytes));
                } catch (Exception e) {
                    System.err.println("❌ Failed to decode base64 credentials: " + e.getMessage());
                    throw e;
                }
            }
            // Try file path (local development)
            else if (credentialsPath != null && !credentialsPath.isEmpty()) {
                System.out.println("✅ Using Firebase credentials from file: " + credentialsPath);
                File credentialsFile = new File(credentialsPath);
                if (!credentialsFile.exists()) {
                    System.err.println("❌ Firebase credentials file not found: " + credentialsPath);
                    firebaseEnabled = false;
                    return;
                }
                credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFile));
            }
            else {
                System.err.println("⚠️  No Firebase credentials found. File upload disabled.");
                firebaseEnabled = false;
                return;
            }

            // Initialize storage
            storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();

            firebaseEnabled = true;
            System.out.println("✅ Firebase Storage initialized successfully!");
            System.out.println("📦 Ready to upload to bucket: " + bucketName);

        } catch (Exception e) {
            System.err.println("❌ Firebase initialization failed:");
            e.printStackTrace();
            firebaseEnabled = false;
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (!firebaseEnabled) {
            System.err.println("❌ Upload rejected: Firebase Storage is not configured");
            throw new RuntimeException("Firebase Storage is not configured. Please contact administrator.");
        }

        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        try {
            // Generate unique filename
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            System.out.println("📤 Uploading file: " + fileName);

            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            // Upload file
            Blob blob = storage.create(blobInfo, file.getBytes());

            // Generate public URL
            String downloadUrl = String.format(
                    "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucketName,
                    fileName.replace("/", "%2F")
            );

            System.out.println("✅ File uploaded successfully: " + downloadUrl);
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
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                System.out.println("✅ File deleted: " + fileName);
            } else {
                System.out.println("⚠️  File not found: " + fileName);
            }
        } catch (Exception e) {
            System.err.println("❌ File deletion failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isEnabled() {
        return firebaseEnabled;
    }

    private String extractFileNameFromUrl(String url) {
        try {
            String[] parts = url.split("/o/");
            if (parts.length > 1) {
                String fileName = parts[1].split("\\?")[0];
                return fileName.replace("%2F", "/");
            }
        } catch (Exception e) {
            System.err.println("⚠️  Failed to extract filename from URL: " + url);
        }
        return "";
    }
}