package com.promptvault.repository;

import com.promptvault.model.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    
    Page<Skill> findByCategory(String category, Pageable pageable);
    
    @Query(value = "SELECT * FROM skills s WHERE s.tags LIKE CONCAT('%,', :tag, ',%')", nativeQuery = true)
    Page<Skill> findByTagsContaining(@Param("tag") String tag, Pageable pageable);
    
    @Query("SELECT s FROM Skill s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Skill> searchByNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    List<Skill> findTop10ByOrderByUsageCountDesc();

    // La skill más usada (por usageCount)
    Optional<Skill> findTopByOrderByUsageCountDesc();
}
