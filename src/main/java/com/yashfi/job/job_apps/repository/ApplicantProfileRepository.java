package com.yashfi.job.job_apps.repository;

import com.yashfi.job.job_apps.model.ApplicantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicantProfileRepository extends JpaRepository<ApplicantProfile, Long> {
    Optional<ApplicantProfile> findByUserId(Long userId);
}