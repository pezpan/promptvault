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
        WorkflowCreateRequest req = WorkflowCreateRequest.builder()
                .name("Code Review Pipeline")
                .description("Revisa código, genera tests unitarios y produce un resumen ejecutivo. " +
                           "3 pasos automáticos con contexto acumulativo.")
                .category("development")
                .inputDescription("Código fuente a revisar (función, clase o fragmento)")
                .outputDescription("Resumen ejecutivo con hallazgos del review y tests generados")
                .build();

        // Paso 1: Code Review con la Skill existente (ID 1 = Code Reviewer Expert)
        StepRequest step1 = StepRequest.builder()
                .name("Análisis de código")
                .stepType(StepType.SKILL)
                .skillId(1L) // Code Reviewer Expert
                .skillParameters(Map.of(
                    "technology_stack", "Java/Spring Boot",
                    "code", "__PREVIOUS_OUTPUT__"
                ))
                .systemInstruction("Eres un senior engineer con 10 años de experiencia en Java. " +
                                   "Sé específico y constructivo en tus observaciones.")
                .build();

        // Paso 2: Generar tests con Skill existente (ID 2 = Test Generator Pro)
        StepRequest step2 = StepRequest.builder()
                .name("Generación de tests unitarios")
                .stepType(StepType.SKILL)
                .skillId(2L) // Test Generator Pro
                .skillParameters(Map.of(
                    "language", "Java",
                    "testing_framework", "JUnit 5",
                    "code_to_test", "__PREVIOUS_OUTPUT__"
                ))
                .systemInstruction("Genera tests que cubran los casos identificados en el análisis previo. " +
                                   "Prioriza los casos edge encontrados.")
                .build();

        // Paso 3: Resumen ejecutivo
        StepRequest step3 = StepRequest.builder()
                .name("Resumen ejecutivo")
                .stepType(StepType.TRANSFORM)
                .transformType(TransformType.SUMMARIZE)
                .systemInstruction("Crea un resumen ejecutivo de máximo 5 puntos clave " +
                                   "para un tech lead no técnico.")
                .build();

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
        WorkflowCreateRequest req = WorkflowCreateRequest.builder()
                .name("Content Processing Pipeline")
                .description("Analiza texto, lo traduce al español y extrae las palabras clave principales.")
                .category("writing")
                .inputDescription("Texto a procesar (artículo, documentación, nota técnica)")
                .outputDescription("Keywords y conceptos clave del contenido traducido")
                .build();

        // Paso 1: Análisis libre
        StepRequest step1 = StepRequest.builder()
                .name("Análisis de contenido")
                .stepType(StepType.FREE_PROMPT)
                .promptTemplate(
                    "Analiza el siguiente texto e identifica: tema principal, tono, audiencia objetivo " +
                    "y estructura. Luego transcribe el texto original íntegro:\n\n" + 
                    "{{PREVIOUS_OUTPUT}}"
                )
                .systemInstruction("Eres un analista de contenido experto en comunicación técnica.")
                .build();

        // Paso 2: Traducir al español
        StepRequest step2 = StepRequest.builder()
                .name("Traducción al español")
                .stepType(StepType.TRANSFORM)
                .transformType(TransformType.TRANSLATE_ES)
                .build();

        // Paso 3: Extraer keywords
        StepRequest step3 = StepRequest.builder()
                .name("Extracción de keywords")
                .stepType(StepType.TRANSFORM)
                .transformType(TransformType.EXTRACT_KEYWORDS)
                .build();

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
        WorkflowCreateRequest req = WorkflowCreateRequest.builder()
                .name("API Documentation Pipeline")
                .description("Documenta un endpoint REST, genera ejemplos de uso y formatea en Markdown listo para publicar.")
                .category("development")
                .inputDescription("Descripción o código del endpoint REST (método, URL, parámetros, respuesta)")
                .outputDescription("Documentación completa en Markdown lista para wiki o README")
                .build();

        // Paso 1: Documentar con Skill (ID 3 = API Documentation Writer)
        StepRequest step1 = StepRequest.builder()
                .name("Documentación técnica")
                .stepType(StepType.SKILL)
                .skillId(3L) // API Documentation Writer
                .skillParameters(Map.of(
                    "http_method", "GET",
                    "endpoint_path", "/api/example",
                    "endpoint_description", "__PREVIOUS_OUTPUT__"
                ))
                .systemInstruction("Sigue el estándar OpenAPI 3.0 para la estructura de la documentación.")
                .build();

        // Paso 2: Generar ejemplos de curl y código
        StepRequest step2 = StepRequest.builder()
                .name("Generación de ejemplos")
                .stepType(StepType.FREE_PROMPT)
                .promptTemplate(
                    "Basándote en la documentación generada, crea ejemplos prácticos de uso:\n" +
                    "1. Ejemplo con curl\n" +
                    "2. Ejemplo con JavaScript (fetch)\n" +
                    "3. Ejemplo con Python (requests)\n" +
                    "4. Ejemplo de respuesta exitosa y de error\n\n" +
                    "Documentación de referencia:\n" +
                    "{{PREVIOUS_OUTPUT}}"
                )
                .systemInstruction("Los ejemplos deben ser ejecutables directamente, con valores realistas.")
                .build();

        // Paso 3: Formatear como Markdown
        StepRequest step3 = StepRequest.builder()
                .name("Formato Markdown final")
                .stepType(StepType.TRANSFORM)
                .transformType(TransformType.FORMAT_MARKDOWN)
                .build();

        req.setSteps(List.of(step1, step2, step3));
        workflowService.create(req);
    }
}
