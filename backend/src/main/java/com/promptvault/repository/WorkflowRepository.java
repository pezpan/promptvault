package com.promptvault.repository;

import com.promptvault.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    List<Workflow> findByCategory(String category);
    List<Workflow> findByNameContainingIgnoreCase(String name);
    List<Workflow> findTop5ByOrderByUsageCountDesc();
}
