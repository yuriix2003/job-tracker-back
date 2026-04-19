package com.yashfi.job.job_apps.dto;

public class StatsResponse {
    // Applicant stats
    private Long totalApplications;
    private Long interviews;
    private Long underReview;
    private Long applied;
    private Long rejected;
    private Long offers;
    private Integer responseRate;

    // Company stats
    private Long activeJobs;
    private Long totalApplicants;
    private Long pendingReview;

    // No-arg constructor
    public StatsResponse() {
    }

    // All-args constructor
    public StatsResponse(Long totalApplications, Long interviews, Long underReview, Long applied,
                         Long rejected, Long offers, Integer responseRate, Long activeJobs,
                         Long totalApplicants, Long pendingReview) {
        this.totalApplications = totalApplications;
        this.interviews = interviews;
        this.underReview = underReview;
        this.applied = applied;
        this.rejected = rejected;
        this.offers = offers;
        this.responseRate = responseRate;
        this.activeJobs = activeJobs;
        this.totalApplicants = totalApplicants;
        this.pendingReview = pendingReview;
    }

    // Getters and Setters
    public Long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(Long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public Long getInterviews() {
        return interviews;
    }

    public void setInterviews(Long interviews) {
        this.interviews = interviews;
    }

    public Long getUnderReview() {
        return underReview;
    }

    public void setUnderReview(Long underReview) {
        this.underReview = underReview;
    }

    public Long getApplied() {
        return applied;
    }

    public void setApplied(Long applied) {
        this.applied = applied;
    }

    public Long getRejected() {
        return rejected;
    }

    public void setRejected(Long rejected) {
        this.rejected = rejected;
    }

    public Long getOffers() {
        return offers;
    }

    public void setOffers(Long offers) {
        this.offers = offers;
    }

    public Integer getResponseRate() {
        return responseRate;
    }

    public void setResponseRate(Integer responseRate) {
        this.responseRate = responseRate;
    }

    public Long getActiveJobs() {
        return activeJobs;
    }

    public void setActiveJobs(Long activeJobs) {
        this.activeJobs = activeJobs;
    }

    public Long getTotalApplicants() {
        return totalApplicants;
    }

    public void setTotalApplicants(Long totalApplicants) {
        this.totalApplicants = totalApplicants;
    }

    public Long getPendingReview() {
        return pendingReview;
    }

    public void setPendingReview(Long pendingReview) {
        this.pendingReview = pendingReview;
    }
}