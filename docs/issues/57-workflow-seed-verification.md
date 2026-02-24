# Issue 57: [WORKFLOWS] Seed data y verificación end-to-end

## Objetivo
Crear 3 workflows de seed predefinidos que demuestren los tres tipos de paso
y el poder del contexto acumulativo. Incluye guía completa de verificación.

---

## Seed data — 3 Workflows predefinidos

Los workflows de seed se crean mejor con un `@Component` con `@PostConstruct`
para evitar problemas con las relaciones JPA al usar SQL directo.

### Crear: `src/main/java/com/promptvault/config/WorkflowSeedData.java`

```java
package com.promptvault.config;

import com.promptvault.dto.WorkflowCreateRequest;
import com.promptvault.dto.WorkflowCreateRequest.StepRequest;
import com.promptvault.model.WorkflowStep.StepType;
import com.promptvault.model.WorkflowStep.TransformType;
import com.promptvault.repository.WorkflowRepository;
import com.promptvault.service.WorkflowService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Profile("dev")  // Solo en desarrollo
@RequiredArgsConstructor
@Slf4j
public class WorkflowSeedData {

    private final WorkflowRepository workflowRepository;
    private final WorkflowService workflowService;

    @PostConstruct
    public void seedWorkflows() {
        if (workflowRepository.count() > 0) {
            log.info("Workflows already seeded, skipping.");
            return;
        }

        log.info("Seeding 3 default workflows...");
        createCodeReviewWorkflow();
        createContentProcessingWorkflow();
        createApiDocumentationWorkflow();
        log.info("Workflow seeding complete.");
    }

    /**
     * Workflow 1: Code Review Pipeline
     * Pasos: SKILL (revisar) → SKILL (generar tests) → TRANSFORM (resumir)
     * Input: código fuente
     * Output: resumen ejecutivo con hallazgos y tests generados
     */
    private void createCodeReviewWorkflow() {
        WorkflowCreateRequest req = new WorkflowCreateRequest();
        req.setName("Code Review Pipeline");
        req.setDescription("Revisa código, genera tests unitarios y produce un resumen ejecutivo. " +
                           "3 pasos automáticos con contexto acumulativo.");
        req.setCategory("development");
        req.setInputDescription("Código fuente a revisar (función, clase o fragmento)");
        req.setOutputDescription("Resumen ejecutivo con hallazgos del review y tests generados");

        // Paso 1: Code Review con la Skill existente (ID 1 = Code Reviewer Expert)
        StepRequest step1 = new StepRequest();
        step1.setName("Análisis de código");
        step1.setStepType(StepType.SKILL);
        step1.setSkillId(1L); // Code Reviewer Expert
        step1.setSkillParameters(Map.of(
            "CODE", "__PREVIOUS_OUTPUT__",  // El input inicial
            "LANGUAGE", "Java"              // Parámetro fijo
        ));
        step1.setSystemInstruction("Eres un senior engineer con 10 años de experiencia en Java. " +
                                   "Sé específico y constructivo en tus observaciones.");

        // Paso 2: Generar tests con Skill existente (ID 2 = Test Generator Pro)
        StepRequest step2 = new StepRequest();
        step2.setName("Generación de tests unitarios");
        step2.setStepType(StepType.SKILL);
        step2.setSkillId(2L); // Test Generator Pro
        step2.setSkillParameters(Map.of(
            "CODE", "__PREVIOUS_OUTPUT__",
            "FRAMEWORK", "JUnit 5"
        ));
        step2.setSystemInstruction("Genera tests que cubran los casos identificados en el análisis previo. " +
                                   "Prioriza los casos edge encontrados.");

        // Paso 3: Resumen ejecutivo
        StepRequest step3 = new StepRequest();
        step3.setName("Resumen ejecutivo");
        step3.setStepType(StepType.TRANSFORM);
        step3.setTransformType(TransformType.SUMMARIZE);
        step3.setSystemInstruction("Crea un resumen ejecutivo de máximo 5 puntos clave " +
                                   "para un tech lead no técnico.");

        req.setSteps(List.of(step1, step2, step3));
        workflowService.create(req);
    }

    /**
     * Workflow 2: Content Processing Pipeline
     * Pasos: FREE_PROMPT (analizar) → TRANSFORM (traducir) → TRANSFORM (keywords)
     * Input: texto en cualquier idioma
     * Output: keywords extraídas del texto traducido
     */
    private void createContentProcessingWorkflow() {
        WorkflowCreateRequest req = new WorkflowCreateRequest();
        req.setName("Content Processing Pipeline");
        req.setDescription("Analiza texto, lo traduce al español y extrae las palabras clave principales.");
        req.setCategory("writing");
        req.setInputDescription("Texto a procesar (artículo, documentación, nota técnica)");
        req.setOutputDescription("Keywords y conceptos clave del contenido traducido");

        // Paso 1: Análisis libre
        StepRequest step1 = new StepRequest();
        step1.setName("Análisis de contenido");
        step1.setStepType(StepType.FREE_PROMPT);
        step1.setPromptTemplate(
            "Analiza el siguiente texto e identifica: tema principal, tono, audiencia objetivo " +
            "y estructura. Luego transcribe el texto original íntegro:\n\n{{PREVIOUS_OUTPUT}}"
        );
        step1.setSystemInstruction("Eres un analista de contenido experto en comunicación técnica.");

        // Paso 2: Traducir al español
        StepRequest step2 = new StepRequest();
        step2.setName("Traducción al español");
        step2.setStepType(StepType.TRANSFORM);
        step2.setTransformType(TransformType.TRANSLATE_ES);

        // Paso 3: Extraer keywords
        StepRequest step3 = new StepRequest();
        step3.setName("Extracción de keywords");
        step3.setStepType(StepType.TRANSFORM);
        step3.setTransformType(TransformType.EXTRACT_KEYWORDS);

        req.setSteps(List.of(step1, step2, step3));
        workflowService.create(req);
    }

    /**
     * Workflow 3: API Documentation Pipeline
     * Pasos: SKILL (documentar API) → FREE_PROMPT (generar ejemplos) → TRANSFORM (formatear)
     * Input: definición de un endpoint REST
     * Output: documentación completa en Markdown con ejemplos
     */
    private void createApiDocumentationWorkflow() {
        WorkflowCreateRequest req = new WorkflowCreateRequest();
        req.setName("API Documentation Pipeline");
        req.setDescription("Documenta un endpoint REST, genera ejemplos de uso y formatea en Markdown listo para publicar.");
        req.setCategory("development");
        req.setInputDescription("Descripción o código del endpoint REST (método, URL, parámetros, respuesta)");
        req.setOutputDescription("Documentación completa en Markdown lista para wiki o README");

        // Paso 1: Documentar con Skill (ID 3 = API Documentation Writer)
        StepRequest step1 = new StepRequest();
        step1.setName("Documentación técnica");
        step1.setStepType(StepType.SKILL);
        step1.setSkillId(3L); // API Documentation Writer
        step1.setSkillParameters(Map.of("ENDPOINT", "__PREVIOUS_OUTPUT__"));
        step1.setSystemInstruction("Sigue el estándar OpenAPI 3.0 para la estructura de la documentación.");

        // Paso 2: Generar ejemplos de curl y código
        StepRequest step2 = new StepRequest();
        step2.setName("Generación de ejemplos");
        step2.setStepType(StepType.FREE_PROMPT);
        step2.setPromptTemplate(
            "Basándote en la documentación generada, crea ejemplos prácticos de uso:\n" +
            "1. Ejemplo con curl\n" +
            "2. Ejemplo con JavaScript (fetch)\n" +
            "3. Ejemplo con Python (requests)\n" +
            "4. Ejemplo de respuesta exitosa y de error\n\n" +
            "Documentación de referencia:\n{{PREVIOUS_OUTPUT}}"
        );
        step2.setSystemInstruction("Los ejemplos deben ser ejecutables directamente, con valores realistas.");

        // Paso 3: Formatear como Markdown
        StepRequest step3 = new StepRequest();
        step3.setName("Formato Markdown final");
        step3.setStepType(StepType.TRANSFORM);
        step3.setTransformType(TransformType.FORMAT_MARKDOWN);

        req.setSteps(List.of(step1, step2, step3));
        workflowService.create(req);
    }
}
```

---

## Guía de verificación end-to-end

### Paso 1 — Compilar y arrancar
```bash
mvn clean compile
mvn spring-boot:run
```
Sin errores de compilación. En los logs debe aparecer:
```
Seeding 3 default workflows...
Workflow seeding complete.
```

### Paso 2 — Verificar listado
```bash
curl http://localhost:8080/api/workflows
```
Esperado: array con 3 workflows.

### Paso 3 — Ejecutar el workflow más sencillo (Content Processing)
Este workflow no usa Skills de la BD, por lo que funciona aunque los IDs
de seed difieran:
```bash
curl -X POST http://localhost:8080/api/workflows/2/execute \
  -H "Content-Type: application/json" \
  -d '{
    "initialInput": "Artificial Intelligence is transforming software development. Developers now use AI tools to write code faster, detect bugs automatically, and generate documentation. This shift requires new skills but also creates new opportunities.",
    "additionalContext": "Contexto: artículo técnico para desarrolladores"
  }'
```

Verificar en la respuesta:
- [ ] `success: true`
- [ ] `completedSteps: 3`
- [ ] `stepResults` tiene 3 elementos
- [ ] `stepResults[0].output` contiene el análisis en inglés
- [ ] `stepResults[1].output` contiene el texto traducido al español
- [ ] `stepResults[2].output` contiene keywords en español
- [ ] `finalOutput` coincide con `stepResults[2].output`
- [ ] `stepResults[1].input` contiene la sección "=== CONTEXTO ACUMULADO ===" (confirma contexto acumulativo)

### Paso 4 — Verificar contexto acumulativo en los inputs
El campo `stepResults[N].input` debe mostrar el contexto completo enviado a Groq.
Para el paso 3 (keywords), debería verse algo como:

```
=== CONTEXTO ACUMULADO ===
--- INPUT INICIAL ---
Artificial Intelligence is transforming...

--- RESULTADO DE: Análisis de contenido ---
[análisis del paso 1]

--- RESULTADO DE: Traducción al español ---
[texto traducido del paso 2]

=== TU TAREA ACTUAL ===
Extrae y lista los conceptos clave...
```

### Paso 5 — Ejecutar Code Review Pipeline (requiere Groq API key)
```bash
curl -X POST http://localhost:8080/api/workflows/1/execute \
  -H "Content-Type: application/json" \
  -d '{
    "initialInput": "public String getUserData(String userId) {\n  String query = \"SELECT * FROM users WHERE id = \" + userId;\n  return db.execute(query);\n}",
    "additionalContext": "Proyecto: API REST Spring Boot, Java 17, base de datos PostgreSQL"
  }'
```

Verificar:
- [ ] `stepResults[0].output` contiene el análisis de código (menciona SQL injection)
- [ ] `stepResults[1].output` contiene tests JUnit 5 que cubren los problemas detectados
- [ ] `stepResults[2].output` es un resumen ejecutivo breve
- [ ] El paso 2 (tests) referencia los problemas encontrados en el paso 1

### Paso 6 — Crear un workflow personalizado desde Swagger
En http://localhost:8080/swagger-ui.html → POST /api/workflows:
```json
{
  "name": "Mi primer workflow",
  "description": "Workflow de prueba con pasos mixtos",
  "category": "test",
  "inputDescription": "Cualquier texto",
  "steps": [
    {
      "name": "Análisis inicial",
      "stepType": "FREE_PROMPT",
      "promptTemplate": "Analiza este texto en 3 puntos clave:\n\n{{PREVIOUS_OUTPUT}}",
      "systemInstruction": "Sé conciso y directo."
    },
    {
      "name": "Traducir resultado",
      "stepType": "TRANSFORM",
      "transformType": "TRANSLATE_EN"
    }
  ]
}
```
- [ ] Se crea con ID nuevo
- [ ] GET /api/workflows muestra ahora 4 workflows
- [ ] POST /api/workflows/{nuevoId}/execute funciona correctamente

---

## Posibles problemas y soluciones

| Problema | Causa | Solución |
|----------|-------|----------|
| `skillId 1 not found` en seed | IDs de Skills son diferentes | Comprobar en H2 Console los IDs reales y actualizar el seed |
| `NullPointerException` en buildSkillPrompt | Skill.getTemplate() devuelve null | Verificar que el campo template no es null en la BD |
| Rate limit de Groq (429) | 3 llamadas en rápida sucesión | Añadir `Thread.sleep(500)` entre pasos en WorkflowExecutionEngine si ocurre |
| Contexto demasiado largo | Workflows de 5+ pasos con outputs grandes | Reducir el inputDescription o usar pasos TRANSFORM (SUMMARIZE) para comprimir antes de continuar |
