package com.yashfi.job.job_apps.repository;

import com.yashfi.job.job_apps.model.Job;
import com.yashfi.job.job_apps.model.Job.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByCompanyId(Long companyId);
    List<Job> findByStatus(JobStatus status);

    @Query("SELECT j FROM Job j WHERE " +
            "LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Job> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT j FROM Job j WHERE " +
            "j.status = 'ACTIVE' AND " +
            "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Job> searchByKeywordAndLocation(@Param("keyword") String keyword,
                                         @Param("location") String location);
}