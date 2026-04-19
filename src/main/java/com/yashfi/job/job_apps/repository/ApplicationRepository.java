package com.yashfi.job.job_apps.repository;

import com.yashfi.job.job_apps.model.Application;
import com.yashfi.job.job_apps.model.Application.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByApplicantId(Long applicantId);
    List<Application> findByJobId(Long jobId);
    boolean existsByJobIdAndApplicantId(Long jobId, Long applicantId);
    Optional<Application> findByJobIdAndApplicantId(Long jobId, Long applicantId);
    long countByApplicantIdAndStatus(Long applicantId, ApplicationStatus status);
    long countByApplicantId(Long applicantId);
    long countByJobId(Long jobId);

    @Query("SELECT a FROM Application a WHERE a.job.company.id = :companyId")
    List<Application> findAllByCompanyId(Long companyId);
}