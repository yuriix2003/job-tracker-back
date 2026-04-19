package com.yashfi.job.job_apps.service;

import com.yashfi.job.job_apps.dto.JobRequest;
import com.yashfi.job.job_apps.dto.JobResponse;
import com.yashfi.job.job_apps.exception.ResourceNotFoundException;
import com.yashfi.job.job_apps.model.Job;
import com.yashfi.job.job_apps.model.User;
import com.yashfi.job.job_apps.repository.JobRepository;
import com.yashfi.job.job_apps.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserService userService;

    public List<JobResponse> getAllJobs(String keyword, String location) {
        List<Job> jobs;

        if (keyword != null && !keyword.isEmpty() && location != null && !location.isEmpty()) {
            jobs = jobRepository.searchByKeywordAndLocation(keyword, location);
        } else if (keyword != null && !keyword.isEmpty()) {
            jobs = jobRepository.searchByKeyword(keyword);
        } else {
            jobs = jobRepository.findByStatus(Job.JobStatus.ACTIVE);
        }

        return jobs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return convertToResponse(job);
    }

    @Transactional
    public JobResponse createJob(JobRequest request, String userEmail) {
        User company = userService.getCurrentUser(userEmail);

        if (company.getRole() != User.UserRole.COMPANY) {
            throw new IllegalArgumentException("Only companies can post jobs");
        }

        Job job = new Job();
        job.setCompany(company);
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setJobType(request.getJobType());
        job.setDeadline(request.getDeadline());
        job.setStatus(Job.JobStatus.ACTIVE);

        Job savedJob = jobRepository.save(job);
        return convertToResponse(savedJob);
    }

    @Transactional
    public JobResponse updateJob(Long id, JobRequest request, String userEmail) {
        User company = userService.getCurrentUser(userEmail);
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("You can only update your own jobs");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setJobType(request.getJobType());
        job.setDeadline(request.getDeadline());

        Job updatedJob = jobRepository.save(job);
        return convertToResponse(updatedJob);
    }

    @Transactional
    public void deleteJob(Long id, String userEmail) {
        User company = userService.getCurrentUser(userEmail);
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("You can only delete your own jobs");
        }

        jobRepository.delete(job);
    }

    public List<JobResponse> getMyJobs(String userEmail) {
        User company = userService.getCurrentUser(userEmail);
        List<Job> jobs = jobRepository.findByCompanyId(company.getId());

        return jobs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private JobResponse convertToResponse(Job job) {
        Long applicantsCount = applicationRepository.countByJobId(job.getId());

        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setCompany(job.getCompany().getName());
        response.setDescription(job.getDescription());
        response.setRequirements(job.getRequirements());
        response.setLocation(job.getLocation());
        response.setSalary(job.getSalary());
        response.setJobType(job.getJobType());
        response.setStatus(job.getStatus().name());
        response.setDeadline(job.getDeadline());
        response.setCreatedAt(job.getCreatedAt());
        response.setApplicantsCount(applicantsCount);

        return response;
    }
}