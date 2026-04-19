package com.yashfi.job.job_apps.dto;

public class ApplicationRequest {
    private String coverLetter;
    private String resumePath;

    // No-arg constructor
    public ApplicationRequest() {
    }

    // All-args constructor
    public ApplicationRequest(String coverLetter, String resumePath) {
        this.coverLetter = coverLetter;
        this.resumePath = resumePath;
    }

    // Getters and Setters
    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public String getResumePath() {
        return resumePath;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }
}