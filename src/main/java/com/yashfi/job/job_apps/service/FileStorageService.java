package com.yashfi.job.job_apps.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${firebase.storage.bucket:}")
    private String bucketName;

    @Value("${firebase.credentials.path:src/main/resources/firebase-service-account.json}")
    private String credentialsPath;

    private Storage storage;
    private boolean firebaseEnabled = false;

    @PostConstruct
    public void initialize() {
        try {
            File credentialsFile = new File(credentialsPath);

            if (!credentialsFile.exists()) {
                System.out.println("⚠️  Firebase credentials not found. File upload will be disabled.");
                System.out.println("   To enable: Add firebase-service-account.json to src/main/resources/");
                firebaseEnabled = false;
                return;
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new FileInputStream(credentialsPath)
            );

            storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();

            firebaseEnabled = true;
            System.out.println("✅ Firebase Storage initialized successfully");

        } catch (Exception e) {
            System.out.println("⚠️  Firebase initialization failed: " + e.getMessage());
            firebaseEnabled = false;
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (!firebaseEnabled) {
            throw new RuntimeException("Firebase Storage is not configured. Please add firebase-service-account.json");
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    }

    public void deleteFile(String fileUrl) {
        if (!firebaseEnabled) {
            return;
        }

        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        BlobId blobId = BlobId.of(bucketName, fileName);
        storage.delete(blobId);
    }

    public boolean isEnabled() {
        return firebaseEnabled;
    }
}