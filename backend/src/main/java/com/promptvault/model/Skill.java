package com.promptvault.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa una "skill" o plantilla de prompt parametrizable.
 * 
 * Las skills son prompts profesionales con placeholders que se pueden
 * personalizar según las necesidades del usuario.
 */
@Entity
@Table(name = "skills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Skill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 50)
    private String category;
    
    @Column(nullable = false, length = 500)
    private String description;
    
    @Column(name = "prompt_template", nullable = false, columnDefinition = "TEXT")
    private String promptTemplate;
    
    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;  // JSON array de parámetros
    
    @Column(name = "example_output", columnDefinition = "TEXT")
    private String exampleOutput;
    
    @Column(name = "tags", length = 500)
    private String tags;
    
    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;
    
    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;  // "beginner", "intermediate", "advanced"
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}