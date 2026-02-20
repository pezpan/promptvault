# Issue 37: [STATS] Crear DTOs para el Dashboard de Estadísticas

## Objetivo
Crear los Data Transfer Objects necesarios para las respuestas del dashboard de estadísticas.

## Archivos a crear

### `src/main/java/com/promptvault/dto/GlobalStatsDTO.java`
```java
package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GlobalStatsDTO {
    private long totalPrompts;
    private long totalImprovements;
    private long totalMcpServers;
    private long totalSkills;
    private double improvementRatio; // 0.0 - 1.0 (porcentaje de prompts mejorados)
}
```

---

### `src/main/java/com/promptvault/dto/PromptStatsDTO.java`
```java
package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class PromptStatsDTO {
    private Map<String, Long> promptsByCategory;
    private long improvedPrompts;
    private long notImprovedPrompts;
}
```

---

### `src/main/java/com/promptvault/dto/RecentImprovementDTO.java`
```java
package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RecentImprovementDTO {
    private Long promptId;
    private String promptTitle;
    private LocalDateTime improvedAt;
}
```

---

### `src/main/java/com/promptvault/dto/AIStatsDTO.java`
```java
package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AIStatsDTO {
    private String mostUsedSkill;
    private String mostPopularMcpServer;
    private long totalAICallsMade;
    private List<RecentImprovementDTO> recentImprovements;
}
```

---

### `src/main/java/com/promptvault/dto/DashboardDTO.java`
```java
package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DashboardDTO {
    private GlobalStatsDTO global;
    private PromptStatsDTO prompts;
    private AIStatsDTO ai;
    private LocalDateTime generatedAt;
}
```

## Notas
- Todos los DTOs usan Lombok `@Builder` y `@Data` para reducir boilerplate
- No hay validaciones en los DTOs de respuesta (son solo para lectura)
- `improvementRatio` es un double entre 0.0 y 1.0 (multiplicar por 100 en el frontend para mostrar %)
