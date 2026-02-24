package com.promptvault.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workflow_steps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Column(nullable = false)
    private int stepOrder; // 1, 2, 3...

    @Column(nullable = false)
    private String name; // Nombre descriptivo del paso

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepType stepType;

    // Solo si stepType = SKILL
    private Long skillId;

    // Parámetros para la skill en formato JSON {"PARAM1": "valor1"}
    // Si el valor es "__PREVIOUS_OUTPUT__", se sustituye por el output del paso anterior
    @Column(columnDefinition = "TEXT")
    private String skillParametersJson;

    // Solo si stepType = FREE_PROMPT
    @Column(columnDefinition = "TEXT")
    private String promptTemplate;

    // Solo si stepType = TRANSFORM
    @Enumerated(EnumType.STRING)
    private TransformType transformType;

    // Instrucción de sistema para este paso (define el ROL del modelo)
    @Column(columnDefinition = "TEXT")
    private String systemInstruction;

    public enum StepType {
        SKILL,        // Usa una Skill del catálogo
        FREE_PROMPT,  // Prompt libre
        TRANSFORM     // Transformación predefinida
    }

    public enum TransformType {
        SUMMARIZE,          // Resumir el output anterior
        TRANSLATE_ES,       // Traducir al español
        TRANSLATE_EN,       // Traducir al inglés
        FORMAT_MARKDOWN,    // Formatear como Markdown estructurado
        EXTRACT_KEYWORDS    // Extraer palabras clave y conceptos principales
    }
}
