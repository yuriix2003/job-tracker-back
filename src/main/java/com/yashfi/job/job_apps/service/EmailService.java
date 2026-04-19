package com.yashfi.job.job_apps.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.*;

import java.util.Collections;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    public void sendApplicationStatusEmail(String recipientEmail, String recipientName,
                                           String jobTitle, String company, String newStatus) {
        try {
            // Configure API client
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKey.setApiKey(brevoApiKey);

            TransactionalEmailsApi api = new TransactionalEmailsApi();

            // Create sender
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(senderEmail);
            sender.setName(senderName);

            // Create recipient
            SendSmtpEmailTo recipient = new SendSmtpEmailTo();
            recipient.setEmail(recipientEmail);
            recipient.setName(recipientName);

            // Email content
            String subject = "Application Status Update: " + jobTitle;
            String htmlContent = buildEmailTemplate(recipientName, jobTitle, company, newStatus);

            // Build email
            SendSmtpEmail email = new SendSmtpEmail();
            email.setSender(sender);
            email.setTo(Collections.singletonList(recipient));
            email.setSubject(subject);
            email.setHtmlContent(htmlContent);

            // Send email
            CreateSmtpEmail result = api.sendTransacEmail(email);
            System.out.println("✅ Email sent successfully: " + result.getMessageId());

        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            // Don't throw exception - email failure shouldn't break the app
        }
    }

    private String buildEmailTemplate(String name, String jobTitle, String company, String status) {
        String statusMessage;
        String statusColor;

        switch (status) {
            case "UNDER_REVIEW":
                statusMessage = "Your application is now under review!";
                statusColor = "#FFA500";
                break;
            case "INTERVIEW":
                statusMessage = "Congratulations! You've been selected for an interview!";
                statusColor = "#4CAF50";
                break;
            case "OFFER":
                statusMessage = "Great news! You've received a job offer!";
                statusColor = "#4CAF50";
                break;
            case "REJECTED":
                statusMessage = "Thank you for your interest. Unfortunately, we've decided to move forward with other candidates.";
                statusColor = "#F44336";
                break;
            default:
                statusMessage = "Your application status has been updated to: " + status;
                statusColor = "#2196F3";
        }

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #2c3e50;'>Application Status Update</h2>" +
                "<p>Hi " + name + ",</p>" +
                "<p style='background-color: " + statusColor + "; color: white; padding: 15px; border-radius: 5px;'>" +
                "<strong>" + statusMessage + "</strong>" +
                "</p>" +
                "<p><strong>Position:</strong> " + jobTitle + "</p>" +
                "<p><strong>Company:</strong> " + company + "</p>" +
                "<p>You can view your application details by logging into your Job Tracker account.</p>" +
                "<hr style='border: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='font-size: 12px; color: #666;'>This is an automated email from Job Tracker. Please do not reply to this email.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}