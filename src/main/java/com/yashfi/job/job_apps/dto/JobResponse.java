package com.yashfi.job.job_apps.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class JobResponse {
    private Long id;
    private String title;
    private String company;
    private String description;
    private String requirements;
    private String location;
    private String salary;
    private String jobType;
    private String status;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private Long applicantsCount;

    // No-arg constructor
    public JobResponse() {
    }

    // All-args constructor
    public JobResponse(Long id, String title, String company, String description, String requirements,
                       String location, String salary, String jobType, String status, LocalDate deadline,
                       LocalDateTime createdAt, Long applicantsCount) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.description = description;
        this.requirements = requirements;
        this.location = location;
        this.salary = salary;
        this.jobType = jobType;
        this.status = status;
        this.deadline = deadline;
        this.createdAt = createdAt;
        this.applicantsCount = applicantsCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getApplicantsCount() {
        return applicantsCount;
    }

    public void setApplicantsCount(Long applicantsCount) {
        this.applicantsCount = applicantsCount;
    }
}