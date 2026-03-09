package com.promptvault.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que almacena el historial de mejoras realizadas por la IA sobre un prompt.
 */
@Entity
@Table(name = "prompt_improvements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptImprovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    @Column(name = "original_content", columnDefinition = "TEXT")
    private String originalContent;

    @Column(name = "improved_content", columnDefinition = "TEXT")
    private String improvedContent;

    /**
     * Lista de mejoras realizadas, guardadas como texto separado por saltos de línea.
     */
    @Column(name = "improvements", columnDefinition = "TEXT")
    private String improvements;

    @Column(name = "quality", length = 50)
    @Builder.Default
    private String quality = "N/A";

    @Column(name = "completeness", nullable = false)
    @Builder.Default
    private Integer completeness = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
