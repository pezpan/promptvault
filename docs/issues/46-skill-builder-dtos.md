# Issue 46: [SKILL-BUILDER] DTOs para generación de Skills con IA

## Objetivo
Crear los DTOs para el endpoint de construcción de Skills personalizadas usando IA.
La idea: el usuario describe qué quiere hacer → la IA genera una Skill completa lista para usar.

## Concepto
El Skill Builder es una capa de meta-IA: usa Groq para generar templates de prompts
que luego se usarán para generar más prompts. Es uno de los ejemplos más claros de
"IA que genera IA" y es muy impactante en demo.

## Archivos a crear

### `src/main/java/com/promptvault/dto/SkillBuildRequest.java`
```java
package com.promptvault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class SkillBuildRequest {

    @NotBlank(message = "La descripción del objetivo es obligatoria")
    @Size(min = 20, max = 500, message = "La descripción debe tener entre 20 y 500 caracteres")
    private String objective;
    // Ejemplo: "Quiero una skill para revisar código Python buscando vulnerabilidades de seguridad"

    @Size(max = 100)
    private String targetAudience;
    // Ejemplo: "Desarrolladores Python con conocimientos de seguridad"

    private List<String> exampleInputs;
    // Ejemplos de inputs que recibirá la skill
    // Ejemplo: ["def login(user, pwd): return db.query(f'SELECT * FROM users WHERE pwd={pwd}')"]

    private String desiredOutputFormat;
    // Ejemplo: "Lista de vulnerabilidades con severidad, descripción y corrección sugerida"

    private String category;
    // Categoría deseada: "development", "security", "writing", etc.

    private boolean saveToDatabase = false;
    // Si true, guarda la skill generada directamente en BD
}
```

### `src/main/java/com/promptvault/dto/SkillBuildResult.java`
```java
package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class SkillBuildResult {
    // La skill generada (puede guardarse directamente con saveToDatabase=true)
    private GeneratedSkill skill;

    // Metadata del proceso de generación
    private String modelUsed;
    private Integer tokensUsed;
    private Long generationTimeMs;

    // Si saveToDatabase=true, el ID asignado
    private Long savedSkillId;

    @Data
    @Builder
    public static class GeneratedSkill {
        private String name;
        private String description;
        private String template;          // El template con {{parámetros}}
        private List<String> parameters;  // Lista de parámetros detectados en el template
        private Map<String, String> parameterDescriptions; // Descripción de cada parámetro
        private String exampleOutput;     // Ejemplo de output generado por la IA
        private String category;
        private int estimatedQualityScore; // 0-100
    }
}
```

## Notas de diseño
- `exampleInputs` es opcional pero mejora mucho la calidad de la skill generada (Few-Shot)
- `saveToDatabase = false` por defecto para que el usuario pueda revisar antes de guardar
- `estimatedQualityScore` lo calcula la propia IA en su respuesta (se le pide en el prompt)
- Los `{{parámetros}}` siguen el mismo formato que las Skills ya implementadas
