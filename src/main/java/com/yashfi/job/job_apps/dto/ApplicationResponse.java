package com.yashfi.job.job_apps.dto;

import java.time.LocalDateTime;

public class ApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String company;
    private String coverLetter;
    private String resumePath;
    private String status;
    private Integer matchScore;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    // No-arg constructor
    public ApplicationResponse() {
    }

    // All-args constructor
    public ApplicationResponse(Long id, Long jobId, String jobTitle, String company, String coverLetter,
                               String resumePath, String status, Integer matchScore, LocalDateTime appliedAt,
                               LocalDateTime updatedAt) {
        this.id = id;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.company = company;
        this.coverLetter = coverLetter;
        this.resumePath = resumePath;
        this.status = status;
        this.matchScore = matchScore;
        this.appliedAt = appliedAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}