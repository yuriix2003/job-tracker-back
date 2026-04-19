package com.yashfi.job.job_apps.repository;

import com.yashfi.job.job_apps.model.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    List<ChatConversation> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<ChatConversation> findByUserIdAndJobId(Long userId, Long jobId);
}