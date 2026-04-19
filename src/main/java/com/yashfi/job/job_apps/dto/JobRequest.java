package com.yashfi.job.job_apps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class JobRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Requirements are required")
    private String requirements;

    @NotBlank(message = "Location is required")
    private String location;

    private String salary;

    @NotBlank(message = "Job type is required")
    private String jobType;

    private LocalDate deadline;

    // No-arg constructor
    public JobRequest() {
    }

    // All-args constructor
    public JobRequest(String title, String description, String requirements, String location,
                      String salary, String jobType, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.requirements = requirements;
        this.location = location;
        this.salary = salary;
        this.jobType = jobType;
        this.deadline = deadline;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}