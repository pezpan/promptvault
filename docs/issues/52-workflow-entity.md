# Issue 52: [WORKFLOWS] Entidad Workflow, WorkflowStep y Repositories

## Concepto
Un Workflow es una secuencia ordenada de pasos que se ejecutan automáticamente,
donde cada paso recibe el contexto acumulado de todos los pasos anteriores.
La definición se guarda en BD y es reutilizable — se puede ejecutar múltiples veces
con diferentes inputs iniciales.

## Modelo de datos

```
Workflow (1) ──────────── (N) WorkflowStep
  - name                        - stepOrder (1, 2, 3...)
  - description                 - name
  - category                    - stepType (SKILL | FREE_PROMPT | TRANSFORM)
  - inputDescription            - skillId (nullable, solo si SKILL)
  - usageCount                  - skillParameters (JSON)
                                - promptTemplate (nullable, solo si FREE_PROMPT)
                                - transformType (nullable, solo si TRANSFORM)
                                - systemInstruction (añade rol/contexto al paso)
```

## Tipos de paso (StepType)

- **SKILL**: Usa una Skill existente del catálogo. Requiere skillId y skillParameters.
- **FREE_PROMPT**: Prompt libre escrito directamente en el paso. Máxima flexibilidad.
- **TRANSFORM**: Transformación predefinida sin llamada configurable: SUMMARIZE, TRANSLATE_ES, TRANSLATE_EN, FORMAT_MARKDOWN, EXTRACT_KEYWORDS.

---

## Archivos a crear

### `src/main/java/com/promptvault/model/WorkflowStep.java`
```java
package com.promptvault.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Map;

@Entity
@Table(name = "workflow_steps")
@Data
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
```

### `src/main/java/com/promptvault/model/Workflow.java`
```java
package com.promptvault.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflows")
@Data
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    private String category;

    // Descripción del input que espera el workflow (para documentación)
    private String inputDescription;

    // Descripción del output final que produce
    private String outputDescription;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("stepOrder ASC")
    private List<WorkflowStep> steps = new ArrayList<>();

    private int usageCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### `src/main/java/com/promptvault/repository/WorkflowRepository.java`
```java
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
```

### `src/main/java/com/promptvault/repository/WorkflowStepRepository.java`
```java
package com.promptvault.repository;

import com.promptvault.model.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {
    List<WorkflowStep> findByWorkflowIdOrderByStepOrderAsc(Long workflowId);
}
```

## Verificación
```bash
mvn clean compile
# Al arrancar, H2 crea las tablas workflows y workflow_steps automáticamente
```
