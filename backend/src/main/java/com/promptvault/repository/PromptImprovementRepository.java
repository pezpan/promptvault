package com.promptvault.repository;

import com.promptvault.model.PromptImprovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptImprovementRepository extends JpaRepository<PromptImprovement, Long> {

    List<PromptImprovement> findTop5ByOrderByCreatedAtDesc();

    List<PromptImprovement> findByPromptIdOrderByCreatedAtDesc(Long promptId);
}
