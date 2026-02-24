# Issue 53: [WORKFLOWS] DTOs de definición y ejecución

## Archivos a crear

### `src/main/java/com/promptvault/dto/WorkflowStepDTO.java`
```java
package com.promptvault.dto;

import com.promptvault.model.WorkflowStep.StepType;
import com.promptvault.model.WorkflowStep.TransformType;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class WorkflowStepDTO {
    private Long id;
    private int stepOrder;
    private String name;
    private StepType stepType;

    // Para SKILL
    private Long skillId;
    private String skillName;           // Nombre de la skill (resuelto)
    private Map<String, String> skillParameters;

    // Para FREE_PROMPT
    private String promptTemplate;

    // Para TRANSFORM
    private TransformType transformType;

    // Común
    private String systemInstruction;
}
```

---

### `src/main/java/com/promptvault/dto/WorkflowDTO.java`
Vista resumida para listados:
```java
package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class WorkflowDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String inputDescription;
    private String outputDescription;
    private int stepCount;
    private int usageCount;
    private List<String> stepNames; // Solo nombres, para preview rápido
}
```

---

### `src/main/java/com/promptvault/dto/WorkflowDetailDTO.java`
Vista completa con todos los pasos:
```java
package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class WorkflowDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String inputDescription;
    private String outputDescription;
    private int usageCount;
    private List<WorkflowStepDTO> steps;
}
```

---

### `src/main/java/com/promptvault/dto/WorkflowCreateRequest.java`
```java
package com.promptvault.dto;

import com.promptvault.model.WorkflowStep.StepType;
import com.promptvault.model.WorkflowStep.TransformType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class WorkflowCreateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;
    private String category;
    private String inputDescription;
    private String outputDescription;

    @NotEmpty(message = "El workflow debe tener al menos un paso")
    private List<StepRequest> steps;

    @Data
    public static class StepRequest {
        @NotBlank
        private String name;

        private StepType stepType = StepType.FREE_PROMPT;

        // Para SKILL
        private Long skillId;
        // Parámetros de la skill. Valor especial "__PREVIOUS_OUTPUT__"
        // indica que ese parámetro se rellena con el output del paso anterior
        private Map<String, String> skillParameters;

        // Para FREE_PROMPT
        // Usar {{PREVIOUS_OUTPUT}} para insertar el output del paso anterior
        private String promptTemplate;

        // Para TRANSFORM
        private TransformType transformType;

        // Común: define el ROL del modelo para este paso
        private String systemInstruction;
    }
}
```

---

### `src/main/java/com/promptvault/dto/WorkflowExecuteRequest.java`
```java
package com.promptvault.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowExecuteRequest {

    @NotBlank(message = "El input inicial es obligatorio")
    private String initialInput;
    // El texto que se pasa al primer paso del workflow
    // Ejemplo: el código fuente a analizar, el texto a procesar, etc.

    private String additionalContext;
    // Contexto adicional opcional que se añade a TODOS los pasos
    // Útil para dar información sobre el proyecto, lenguaje, etc.
}
```

---

### `src/main/java/com/promptvault/dto/WorkflowExecutionResult.java`
```java
package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WorkflowExecutionResult {
    private Long workflowId;
    private String workflowName;
    private boolean success;

    // El output final (último paso)
    private String finalOutput;

    // Outputs intermedios de cada paso
    private List<StepResult> stepResults;

    // Métricas de ejecución
    private int totalSteps;
    private int completedSteps;
    private long totalExecutionTimeMs;
    private int totalTokensUsed; // estimado

    private LocalDateTime executedAt;
    private String errorMessage; // Solo si success = false

    @Data
    @Builder
    public static class StepResult {
        private int stepOrder;
        private String stepName;
        private String stepType;    // SKILL / FREE_PROMPT / TRANSFORM
        private String input;       // El prompt completo enviado a Groq (con contexto acumulado)
        private String output;      // La respuesta de Groq
        private long executionTimeMs;
        private boolean success;
        private String errorMessage;
    }
}
```

## Notas de diseño

- `WorkflowExecuteRequest.initialInput` es el único dato que varía entre ejecuciones.
  La definición de los pasos (qué skills usar, qué prompts) está en el Workflow guardado en BD.
- `StepResult.input` almacena el prompt completo enviado a Groq incluyendo el contexto
  acumulado — útil para debug y para entender cómo funciona el chain internamente.
- `totalTokensUsed` es una estimación basada en caracteres (÷ 4 ≈ tokens) ya que Groq
  no siempre devuelve el usage en la respuesta.

## Verificación
```bash
mvn clean compile
```
