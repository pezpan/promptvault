package com.promptvault.repository;

import com.promptvault.model.ContextPack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContextPackRepository extends JpaRepository<ContextPack, Long> {
    List<ContextPack> findByCategory(String category);
    List<ContextPack> findByNameContainingIgnoreCase(String name);
    List<ContextPack> findTop5ByOrderByUsageCountDesc();
}
