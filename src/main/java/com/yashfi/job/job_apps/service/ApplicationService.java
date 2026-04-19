package com.yashfi.job.job_apps.service;

import com.yashfi.job.job_apps.dto.ApplicationRequest;
import com.yashfi.job.job_apps.dto.ApplicationResponse;
import com.yashfi.job.job_apps.exception.ResourceNotFoundException;
import com.yashfi.job.job_apps.model.Application;
import com.yashfi.job.job_apps.model.Job;
import com.yashfi.job.job_apps.model.User;
import com.yashfi.job.job_apps.repository.ApplicationRepository;
import com.yashfi.job.job_apps.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public ApplicationResponse applyToJob(Long jobId, ApplicationRequest request, String userEmail) {
        User applicant = userService.getCurrentUser(userEmail);

        if (applicant.getRole() != User.UserRole.APPLICANT) {
            throw new IllegalArgumentException("Only applicants can apply to jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (applicationRepository.existsByJobIdAndApplicantId(jobId, applicant.getId())) {
            throw new IllegalArgumentException("You have already applied to this job");
        }

        Application application = new Application();
        application.setJob(job);
        application.setApplicant(applicant);
        application.setCoverLetter(request.getCoverLetter());
        application.setResumePath(request.getResumePath());
        application.setStatus(Application.ApplicationStatus.APPLIED);

        Application savedApplication = applicationRepository.save(application);
        return convertToResponse(savedApplication);
    }

    public List<ApplicationResponse> getMyApplications(String userEmail) {
        User applicant = userService.getCurrentUser(userEmail);
        List<Application> applications = applicationRepository.findByApplicantId(applicant.getId());

        return applications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getApplicationsForJob(Long jobId, String userEmail) {
        User company = userService.getCurrentUser(userEmail);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("You can only view applications for your own jobs");
        }

        List<Application> applications = applicationRepository.findByJobId(jobId);

        return applications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // In ApplicationService.java, update the updateApplicationStatus method:

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, String status, String userEmail) {
        User company = userService.getCurrentUser(userEmail);
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getJob().getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("You can only update applications for your own jobs");
        }

        application.setStatus(Application.ApplicationStatus.valueOf(status));
        Application updatedApplication = applicationRepository.save(application);

        // Send email notification
        try {
            emailService.sendApplicationStatusEmail(
                    updatedApplication.getApplicant().getEmail(),
                    updatedApplication.getApplicant().getName(),
                    updatedApplication.getJob().getTitle(),
                    updatedApplication.getJob().getCompany().getName(),
                    status
            );
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }

        return convertToResponse(updatedApplication);
    }

    public ApplicationResponse getApplicationById(Long id, String userEmail) {
        User user = userService.getCurrentUser(userEmail);
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        boolean isApplicant = application.getApplicant().getId().equals(user.getId());
        boolean isCompany = application.getJob().getCompany().getId().equals(user.getId());

        if (!isApplicant && !isCompany) {
            throw new IllegalArgumentException("You don't have access to this application");
        }

        return convertToResponse(application);
    }

    private ApplicationResponse convertToResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setJobId(application.getJob().getId());
        response.setJobTitle(application.getJob().getTitle());
        response.setCompany(application.getJob().getCompany().getName());
        response.setCoverLetter(application.getCoverLetter());
        response.setResumePath(application.getResumePath());
        response.setStatus(application.getStatus().name());
        response.setMatchScore(application.getMatchScore());
        response.setAppliedAt(application.getAppliedAt());
        response.setUpdatedAt(application.getUpdatedAt());

        return response;
    }
}