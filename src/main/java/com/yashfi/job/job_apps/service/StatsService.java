package com.yashfi.job.job_apps.service;

import com.yashfi.job.job_apps.dto.StatsResponse;
import com.yashfi.job.job_apps.model.Application;
import com.yashfi.job.job_apps.model.Job;
import com.yashfi.job.job_apps.model.User;
import com.yashfi.job.job_apps.repository.ApplicationRepository;
import com.yashfi.job.job_apps.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserService userService;

    public StatsResponse getApplicantStats(String userEmail) {
        User applicant = userService.getCurrentUser(userEmail);

        long total = applicationRepository.countByApplicantId(applicant.getId());
        long interviews = applicationRepository.countByApplicantIdAndStatus(
                applicant.getId(), Application.ApplicationStatus.INTERVIEW);
        long underReview = applicationRepository.countByApplicantIdAndStatus(
                applicant.getId(), Application.ApplicationStatus.UNDER_REVIEW);
        long applied = applicationRepository.countByApplicantIdAndStatus(
                applicant.getId(), Application.ApplicationStatus.APPLIED);
        long rejected = applicationRepository.countByApplicantIdAndStatus(
                applicant.getId(), Application.ApplicationStatus.REJECTED);
        long offers = applicationRepository.countByApplicantIdAndStatus(
                applicant.getId(), Application.ApplicationStatus.OFFER);

        int responseRate = total > 0 ?
                (int) (((total - applied) * 100.0) / total) : 0;

        StatsResponse response = new StatsResponse();
        response.setTotalApplications(total);
        response.setInterviews(interviews);
        response.setUnderReview(underReview);
        response.setApplied(applied);
        response.setRejected(rejected);
        response.setOffers(offers);
        response.setResponseRate(responseRate);

        return response;
    }

    public StatsResponse getCompanyStats(String userEmail) {
        User company = userService.getCurrentUser(userEmail);

        long activeJobs = jobRepository.findByCompanyId(company.getId()).stream()
                .filter(job -> job.getStatus() == Job.JobStatus.ACTIVE)
                .count();

        long totalApplicants = applicationRepository.findAllByCompanyId(company.getId()).size();

        long pendingReview = applicationRepository.findAllByCompanyId(company.getId()).stream()
                .filter(app -> app.getStatus() == Application.ApplicationStatus.APPLIED)
                .count();

        StatsResponse response = new StatsResponse();
        response.setActiveJobs(activeJobs);
        response.setTotalApplicants(totalApplicants);
        response.setPendingReview(pendingReview);

        return response;
    }
}