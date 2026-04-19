package com.yashfi.job.job_apps.repository;

import com.yashfi.job.job_apps.model.AIAnalysis;
import com.yashfi.job.job_apps.model.AIAnalysis.AnalysisType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIAnalysisRepository extends JpaRepository<AIAnalysis, Long> {

    List<AIAnalysis> findByApplicationId(Long applicationId);
    Optional<AIAnalysis> findByApplicationIdAndAnalysisType(Long applicationId, AnalysisType analysisType);
}