# ISSUES 32-35: Sistema de Skills (Plantillas de Prompts)

## ISSUE 32: Crear Entidad Skill y Repository

**Archivo 1**: `backend/src/main/java/com/promptvault/model/Skill.java`

```java
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
    
    @Column(name = "tags")
    private String[] tags;
    
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
```

**Archivo 2**: `backend/src/main/java/com/promptvault/repository/SkillRepository.java`

```java
package com.promptvault.repository;

import com.promptvault.model.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    
    Page<Skill> findByCategory(String category, Pageable pageable);
    
    @Query("SELECT s FROM Skill s WHERE :tag = ANY(s.tags)")
    Page<Skill> findByTagsContaining(@Param("tag") String tag, Pageable pageable);
    
    @Query("SELECT s FROM Skill s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Skill> searchByNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    List<Skill> findTop10ByOrderByUsageCountDesc();
}
```

---

## ISSUE 33: DTOs y Service para Skills

**Archivo 1**: `backend/src/main/java/com/promptvault/dto/SkillDTO.java`

```java
package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDTO {
    private Long id;
    private String name;
    private String category;
    private String description;
    private String promptTemplate;
    private List<SkillParameter> parameters;
    private String exampleOutput;
    private List<String> tags;
    private Integer usageCount;
    private String difficultyLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillParameter {
        private String name;
        private String type;  // "text", "select", "multiselect", "number"
        private String description;
        private List<String> options;  // Para select/multiselect
        private String defaultValue;
        private Boolean required;
    }
}
```

**Archivo 2**: `backend/src/main/java/com/promptvault/dto/GeneratePromptRequest.java`

```java
package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePromptRequest {
    private Map<String, Object> parameters;
}
```

**Archivo 3**: `backend/src/main/java/com/promptvault/service/SkillService.java`

```java
package com.promptvault.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptvault.dto.GeneratePromptRequest;
import com.promptvault.dto.SkillDTO;
import com.promptvault.exception.ResourceNotFoundException;
import com.promptvault.model.Skill;
import com.promptvault.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {
    
    private final SkillRepository skillRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional(readOnly = true)
    public Page<SkillDTO> getAllSkills(Pageable pageable) {
        return skillRepository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public SkillDTO getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", id));
        return toDTO(skill);
    }
    
    @Transactional(readOnly = true)
    public Page<SkillDTO> getSkillsByCategory(String category, Pageable pageable) {
        return skillRepository.findByCategory(category, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public List<SkillDTO> getPopularSkills() {
        return skillRepository.findTop10ByOrderByUsageCountDesc()
            .stream()
            .map(this::toDTO)
            .toList();
    }
    
    /**
     * Genera un prompt personalizado a partir de una skill y parámetros.
     */
    @Transactional
    public String generatePrompt(Long skillId, GeneratePromptRequest request) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillId));
        
        String template = skill.getPromptTemplate();
        
        // Reemplazar placeholders con valores
        for (Map.Entry<String, Object> entry : request.getParameters().entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue().toString();
            template = template.replace(placeholder, value);
        }
        
        // Incrementar contador
        skill.setUsageCount(skill.getUsageCount() + 1);
        skillRepository.save(skill);
        
        log.info("Prompt generado desde skill '{}' con {} parámetros", skill.getName(), request.getParameters().size());
        
        return template;
    }
    
    private List<SkillDTO.SkillParameter> parseParameters(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<SkillDTO.SkillParameter>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parseando parámetros", e);
            return List.of();
        }
    }
    
    private SkillDTO toDTO(Skill skill) {
        return SkillDTO.builder()
            .id(skill.getId())
            .name(skill.getName())
            .category(skill.getCategory())
            .description(skill.getDescription())
            .promptTemplate(skill.getPromptTemplate())
            .parameters(parseParameters(skill.getParameters()))
            .exampleOutput(skill.getExampleOutput())
            .tags(skill.getTags() != null ? List.of(skill.getTags()) : List.of())
            .usageCount(skill.getUsageCount())
            .difficultyLevel(skill.getDifficultyLevel())
            .createdAt(skill.getCreatedAt())
            .updatedAt(skill.getUpdatedAt())
            .build();
    }
}
```

---

## ISSUE 34: Controller REST para Skills

**Archivo**: `backend/src/main/java/com/promptvault/controller/SkillController.java`

```java
package com.promptvault.controller;

import com.promptvault.dto.GeneratePromptRequest;
import com.promptvault.dto.SkillDTO;
import com.promptvault.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "API para skills (plantillas de prompts)")
public class SkillController {
    
    private final SkillService skillService;
    
    @GetMapping
    @Operation(summary = "Listar todas las skills")
    public ResponseEntity<Page<SkillDTO>> getAllSkills(
        @PageableDefault(size = 20, sort = "usageCount", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(required = false) String category
    ) {
        Page<SkillDTO> skills = category != null && !category.isBlank()
            ? skillService.getSkillsByCategory(category, pageable)
            : skillService.getAllSkills(pageable);
        
        return ResponseEntity.ok(skills);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una skill por ID")
    public ResponseEntity<SkillDTO> getSkillById(@PathVariable Long id) {
        SkillDTO skill = skillService.getSkillById(id);
        return ResponseEntity.ok(skill);
    }
    
    @GetMapping("/popular")
    @Operation(summary = "Obtener las skills más populares")
    public ResponseEntity<List<SkillDTO>> getPopularSkills() {
        List<SkillDTO> popular = skillService.getPopularSkills();
        return ResponseEntity.ok(popular);
    }
    
    @PostMapping("/{id}/generate")
    @Operation(summary = "Generar prompt personalizado desde una skill")
    public ResponseEntity<Map<String, String>> generatePrompt(
        @PathVariable Long id,
        @RequestBody GeneratePromptRequest request
    ) {
        String generatedPrompt = skillService.generatePrompt(id, request);
        return ResponseEntity.ok(Map.of("generatedPrompt", generatedPrompt));
    }
}
```

---

## ISSUE 35: Seed Data - 5 Skills Profesionales

**Archivo**: `backend/src/main/resources/skills-data.sql`

```sql
-- Seed data para Skills predefinidas

-- 1. Code Reviewer Expert
INSERT INTO skills (name, category, description, prompt_template, parameters, example_output, tags, usage_count, difficulty_level) VALUES
('Code Reviewer Expert', 'code-quality', 
'Revisa código con enfoque profesional en seguridad, performance y best practices',
'[ROL]
Actúa como Senior Code Reviewer con {years_experience} años de experiencia en {technology_stack}.

[TAREA]
Revisa el siguiente código enfocándote en:
{review_aspects}

CÓDIGO A REVISAR:
```
{code}
```

[AUDIENCIA]
Desarrolladores que necesitan feedback constructivo y accionable.

[FORMATO]
{output_format}

[CONTEXTO/DETALLES]
- Tecnología: {technology_stack}
- Nivel del equipo: {team_level}
- Prioridad: {priority_focus}',
'[
  {"name": "technology_stack", "type": "select", "description": "Stack tecnológico", "options": ["Java/Spring Boot", "JavaScript/React", "Python/Django", "TypeScript/Node.js"], "required": true},
  {"name": "years_experience", "type": "select", "options": ["5", "10", "15", "20+"], "defaultValue": "10", "required": true},
  {"name": "review_aspects", "type": "multiselect", "options": ["Seguridad", "Performance", "Legibilidad", "Testing", "Arquitectura"], "defaultValue": "Seguridad,Performance", "required": true},
  {"name": "team_level", "type": "select", "options": ["Junior", "Mid", "Senior"], "defaultValue": "Mid", "required": true},
  {"name": "priority_focus", "type": "select", "options": ["Seguridad", "Performance", "Mantenibilidad"], "defaultValue": "Seguridad", "required": true},
  {"name": "output_format", "type": "select", "options": ["Lista detallada con ejemplos", "Tabla comparativa", "Código corregido con comentarios"], "defaultValue": "Lista detallada con ejemplos", "required": true},
  {"name": "code", "type": "text", "description": "Código a revisar", "required": true}
]',
'### Issues Encontrados
1. [CRITICAL] SQL Injection vulnerability en línea 45...',
ARRAY['code-review', 'quality', 'security'],
0,
'intermediate'),

-- 2. Test Generator Pro
INSERT INTO skills (name, category, description, prompt_template, parameters, example_output, tags, usage_count, difficulty_level) VALUES
('Test Generator Pro', 'testing',
'Genera tests unitarios completos con cobertura exhaustiva',
'[ROL]
Actúa como QA Engineer especializado en {testing_framework}.

[TAREA]
Genera tests unitarios para esta función/método:

```{language}
{code_to_test}
```

Incluye tests para:
1. Happy path (casos normales)
2. Edge cases (límites, valores extremos)
3. Error handling (excepciones, validaciones)
4. {additional_scenarios}

[AUDIENCIA]
Desarrolladores que practican TDD y buscan alta cobertura.

[FORMATO]
- Tests con nombres descriptivos
- Arrange-Act-Assert pattern
- Mocks apropiados
- Assertions claras

[CONTEXTO/DETALLES]
- Framework: {testing_framework}
- Lenguaje: {language}
- Cobertura objetivo: {coverage_target}%',
'[
  {"name": "language", "type": "select", "options": ["Java", "JavaScript", "Python", "TypeScript"], "required": true},
  {"name": "testing_framework", "type": "select", "options": ["JUnit 5", "Jest", "pytest", "Mocha"], "required": true},
  {"name": "coverage_target", "type": "select", "options": ["70", "80", "90", "100"], "defaultValue": "80", "required": true},
  {"name": "additional_scenarios", "type": "text", "description": "Escenarios adicionales a testear", "required": false},
  {"name": "code_to_test", "type": "text", "description": "Código a testear", "required": true}
]',
'@Test
void shouldCalculateDiscount_whenValidInput() {...}',
ARRAY['testing', 'tdd', 'quality'],
0,
'intermediate'),

-- 3. API Documentation Writer
INSERT INTO skills (name, category, description, prompt_template, parameters, example_output, tags, usage_count, difficulty_level) VALUES
('API Documentation Writer', 'documentation',
'Genera documentación completa y profesional para APIs REST',
'[ROL]
Actúa como Technical Writer especializado en documentación de APIs.

[TAREA]
Documenta el siguiente endpoint REST:

METHOD: {http_method}
PATH: {endpoint_path}
DESCRIPCIÓN: {endpoint_description}

[AUDIENCIA]
{audience_type}

[FORMATO]
Genera documentación en formato {doc_format} que incluya:
- Descripción clara del endpoint
- Parámetros de entrada con tipos y validaciones
- Ejemplos de requests
- Ejemplos de responses (éxito y errores)
- Códigos de estado HTTP posibles
- {additional_sections}

[CONTEXTO/DETALLES]
- Autenticación: {auth_type}
- Rate limiting: {rate_limit}
- Versión API: {api_version}',
'[
  {"name": "http_method", "type": "select", "options": ["GET", "POST", "PUT", "DELETE", "PATCH"], "required": true},
  {"name": "endpoint_path", "type": "text", "description": "Ruta del endpoint (ej: /api/users/{id})", "required": true},
  {"name": "endpoint_description", "type": "text", "description": "Qué hace el endpoint", "required": true},
  {"name": "audience_type", "type": "select", "options": ["Desarrolladores externos", "Equipo interno", "Clientes técnicos"], "defaultValue": "Desarrolladores externos", "required": true},
  {"name": "doc_format", "type": "select", "options": ["OpenAPI/Swagger", "Markdown", "Postman Collection"], "defaultValue": "Markdown", "required": true},
  {"name": "auth_type", "type": "select", "options": ["Bearer Token", "API Key", "OAuth2", "None"], "defaultValue": "Bearer Token", "required": true},
  {"name": "rate_limit", "type": "text", "defaultValue": "100 requests/hour", "required": false},
  {"name": "api_version", "type": "text", "defaultValue": "v1", "required": false},
  {"name": "additional_sections", "type": "text", "description": "Secciones adicionales a incluir", "required": false}
]',
'## GET /api/users/{id}
Obtiene información de un usuario...',
ARRAY['documentation', 'api', 'rest'],
0,
'beginner'),

-- 4. Refactoring Assistant
INSERT INTO skills (name, category, description, prompt_template, parameters, example_output, tags, usage_count, difficulty_level) VALUES
('Refactoring Assistant', 'refactoring',
'Sugiere mejoras de código aplicando principios SOLID y clean code',
'[ROL]
Actúa como Software Architect especializado en clean code y principios SOLID.

[TAREA]
Analiza este código y propón refactorings:

```{language}
{code_to_refactor}
```

Enfócate en:
{refactoring_focus}

[AUDIENCIA]
Desarrolladores que buscan mejorar la calidad y mantenibilidad del código.

[FORMATO]
Para cada refactoring propuesto:
1. Identificar el code smell o problema
2. Explicar por qué es un problema
3. Mostrar el código refactorizado
4. Beneficios de la mejora

[CONTEXTO/DETALLES]
- Lenguaje: {language}
- Principios a aplicar: {principles}
- Nivel de agresividad: {aggressiveness}',
'[
  {"name": "language", "type": "select", "options": ["Java", "JavaScript", "Python", "C#", "TypeScript"], "required": true},
  {"name": "refactoring_focus", "type": "multiselect", "options": ["Extract Method", "Remove Duplication", "Simplify Conditionals", "Improve Names", "Apply Design Patterns"], "required": true},
  {"name": "principles", "type": "multiselect", "options": ["SOLID", "DRY", "KISS", "YAGNI", "Clean Code"], "defaultValue": "SOLID,DRY", "required": true},
  {"name": "aggressiveness", "type": "select", "options": ["Conservador", "Moderado", "Agresivo"], "defaultValue": "Moderado", "description": "Qué tan drásticos son los cambios", "required": true},
  {"name": "code_to_refactor", "type": "text", "description": "Código a refactorizar", "required": true}
]',
'### Refactoring 1: Extract Method
PROBLEMA: Método demasiado largo (50 líneas)...',
ARRAY['refactoring', 'clean-code', 'solid'],
0,
'advanced'),

-- 5. Bug Hunter
INSERT INTO skills (name, category, description, prompt_template, parameters, example_output, tags, usage_count, difficulty_level) VALUES
('Bug Hunter', 'debugging',
'Identifica y diagnostica bugs de forma sistemática',
'[ROL]
Actúa como Expert Debugger con {years_experience} años encontrando bugs complejos en {technology}.

[TAREA]
Analiza este código que presenta el siguiente comportamiento inesperado:

SÍNTOMA: {bug_symptom}

CÓDIGO:
```{language}
{buggy_code}
```

CONTEXTO:
{execution_context}

Realiza:
1. Análisis sistemático del código
2. Hipótesis sobre la causa
3. Identificación del bug
4. Solución propuesta
5. Cómo prevenir bugs similares

[AUDIENCIA]
Desarrollador que necesita entender no solo la solución sino el proceso de debugging.

[FORMATO]
{output_style}

[CONTEXTO/DETALLES]
- Tecnología: {technology}
- Severidad: {severity}
- Frecuencia: {frequency}',
'[
  {"name": "technology", "type": "select", "options": ["Spring Boot", "React", "Node.js", "Django", "Angular"], "required": true},
  {"name": "language", "type": "select", "options": ["Java", "JavaScript", "Python", "TypeScript"], "required": true},
  {"name": "years_experience", "type": "select", "options": ["5", "10", "15"], "defaultValue": "10", "required": true},
  {"name": "bug_symptom", "type": "text", "description": "Qué comportamiento incorrecto se observa", "required": true},
  {"name": "execution_context", "type": "text", "description": "Cuándo/cómo ocurre el bug", "required": true},
  {"name": "severity", "type": "select", "options": ["Critical", "High", "Medium", "Low"], "defaultValue": "High", "required": true},
  {"name": "frequency", "type": "select", "options": ["Siempre", "Frecuente", "Ocasional", "Raro"], "required": true},
  {"name": "output_style", "type": "select", "options": ["Paso a paso detallado", "Tabla diagnóstico", "Narrativo"], "defaultValue": "Paso a paso detallado", "required": true},
  {"name": "buggy_code", "type": "text", "description": "Código con el bug", "required": true}
]',
'### Análisis del Bug
HIPÓTESIS: Race condition en operación async...',
ARRAY['debugging', 'troubleshooting', 'analysis'],
0,
'advanced');
```

---

## Verificación Final Issues 32-35

```bash
mvn clean compile
mvn spring-boot:run

# Probar en Swagger:
# - GET /api/skills (listar - debe haber 5 skills)
# - GET /api/skills/1 (ver Code Reviewer Expert)
# - POST /api/skills/1/generate (generar prompt personalizado)
#   Body ejemplo:
#   {
#     "parameters": {
#       "technology_stack": "Java/Spring Boot",
#       "years_experience": "10",
#       "review_aspects": "Seguridad,Performance",
#       "team_level": "Mid",
#       "priority_focus": "Seguridad",
#       "output_format": "Lista detallada con ejemplos",
#       "code": "public void processPayment(String cardNumber) { ... }"
#     }
#   }
```

