package com.promptvault.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptvault.dto.WorkflowExecuteRequest;
import com.promptvault.dto.WorkflowExecutionResult;
import com.promptvault.dto.WorkflowExecutionResult.StepResult;
import com.promptvault.model.Skill;
import com.promptvault.model.WorkflowStep;
import com.promptvault.model.WorkflowStep.StepType;
import com.promptvault.model.WorkflowStep.TransformType;
import com.promptvault.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionEngine {

    private final GroqClient groqClient;
    private final SkillRepository skillRepository;
    private final ObjectMapper objectMapper;

    /**
     * Ejecuta todos los pasos de un workflow en secuencia.
     * El contexto se acumula: cada paso recibe todos los outputs anteriores.
     */
    public WorkflowExecutionResult execute(
            Long workflowId,
            String workflowName,
            List<WorkflowStep> steps,
            WorkflowExecuteRequest request) {

        Instant globalStart = Instant.now();
        List<StepResult> stepResults = new ArrayList<>();

        // El contexto acumulado crece en cada paso
        List<String[]> accumulatedContext = new ArrayList<>();
        // Cada entrada: [nombrePaso, outputPaso]

        String lastOutput = request.getInitialInput();
        boolean allSucceeded = true;
        String errorMessage = null;

        for (WorkflowStep step : steps) {
            Instant stepStart = Instant.now();
            log.info("Executing workflow {} - step {}: {}", workflowId, step.getStepOrder(), step.getName());

            try {
                // 1. Construir el prompt completo con contexto acumulado
                String fullPrompt = buildAccumulativePrompt(
                    step, request.getInitialInput(), accumulatedContext,
                    lastOutput, request.getAdditionalContext()
                );

                // 2. Llamar a Groq (usando generateContent)
                String output = groqClient.generateContent(fullPrompt);
                long stepTime = Instant.now().toEpochMilli() - stepStart.toEpochMilli();

                // 3. Acumular el contexto
                accumulatedContext.add(new String[]{ step.getName(), output });
                lastOutput = output;

                stepResults.add(StepResult.builder()
                        .stepOrder(step.getStepOrder())
                        .stepName(step.getName())
                        .stepType(step.getStepType().name())
                        .input(fullPrompt)
                        .output(output)
                        .executionTimeMs(stepTime)
                        .success(true)
                        .build());

            } catch (Exception e) {
                log.error("Error in step {} of workflow {}: {}", step.getStepOrder(), workflowId, e.getMessage());
                allSucceeded = false;
                errorMessage = "Error en el paso " + step.getStepOrder() + " (" + step.getName() + "): " + e.getMessage();

                stepResults.add(StepResult.builder()
                        .stepOrder(step.getStepOrder())
                        .stepName(step.getName())
                        .stepType(step.getStepType().name())
                        .input("")
                        .output("")
                        .executionTimeMs(Instant.now().toEpochMilli() - stepStart.toEpochMilli())
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build());

                break; // Detener la cadena si un paso falla
            }
        }

        long totalTime = Instant.now().toEpochMilli() - globalStart.toEpochMilli();
        int estimatedTokens = stepResults.stream()
                .mapToInt(r -> (r.getInput().length() + r.getOutput().length()) / 4)
                .sum();

        return WorkflowExecutionResult.builder()
                .workflowId(workflowId)
                .workflowName(workflowName)
                .success(allSucceeded)
                .finalOutput(lastOutput)
                .stepResults(stepResults)
                .totalSteps(steps.size())
                .completedSteps((int) stepResults.stream().filter(StepResult::isSuccess).count())
                .totalExecutionTimeMs(totalTime)
                .totalTokensUsed(estimatedTokens)
                .executedAt(LocalDateTime.now())
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Construye el prompt completo con contexto acumulativo para un paso.
     * Este método es el núcleo del sistema — define cómo "ve" cada paso
     * todo lo que ha ocurrido antes en la cadena.
     */
    private String buildAccumulativePrompt(
            WorkflowStep step,
            String initialInput,
            List<String[]> accumulatedContext,
            String previousOutput,
            String additionalContext) {

        StringBuilder prompt = new StringBuilder();

        // 1. System instruction del paso (define el ROL)
        if (step.getSystemInstruction() != null && !step.getSystemInstruction().isBlank()) {
            prompt.append(step.getSystemInstruction()).append("\n\n");
        }

        // 2. Contexto acumulado (todos los pasos anteriores)
        if (!accumulatedContext.isEmpty() || (additionalContext != null && !additionalContext.isBlank())) {
            prompt.append("=== CONTEXTO ACUMULADO ===\n");

            if (additionalContext != null && !additionalContext.isBlank()) {
                prompt.append("--- CONTEXTO ADICIONAL ---\n")
                      .append(additionalContext).append("\n\n");
            }

            prompt.append("--- INPUT INICIAL ---\n")
                  .append(initialInput).append("\n\n");

            for (String[] ctx : accumulatedContext) {
                prompt.append("--- RESULTADO DE: ").append(ctx[0]).append(" ---\n")
                      .append(ctx[1]).append("\n\n");
            }

            prompt.append("=== TU TAREA ACTUAL ===\n");
        }

        // 3. El prompt específico del paso según su tipo
        String stepPrompt = buildStepPrompt(step, previousOutput);
        prompt.append(stepPrompt);

        return prompt.toString();
    }

    /**
     * Construye el prompt específico según el tipo de paso.
     */
    private String buildStepPrompt(WorkflowStep step, String previousOutput) {
        return switch (step.getStepType()) {

            case SKILL -> buildSkillPrompt(step, previousOutput);

            case FREE_PROMPT -> {
                String template = step.getPromptTemplate() != null
                        ? step.getPromptTemplate() : "";
                // Sustituir {{PREVIOUS_OUTPUT}} en el template libre
                yield template.replace("{{PREVIOUS_OUTPUT}}", previousOutput);
            }

            case TRANSFORM -> buildTransformPrompt(step.getTransformType(), previousOutput);
        };
    }

    private String buildSkillPrompt(WorkflowStep step, String previousOutput) {
        if (step.getSkillId() == null) {
            return "Analiza el siguiente contenido y proporciona una respuesta útil:\n\n" + previousOutput;
        }

        Optional<Skill> skillOpt = skillRepository.findById(step.getSkillId());
        if (skillOpt.isEmpty()) {
            log.warn("Skill {} not found for workflow step, using fallback", step.getSkillId());
            return "Analiza el siguiente contenido:\n\n" + previousOutput;
        }

        Skill skill = skillOpt.get();
        String template = skill.getPromptTemplate();

        // Sustituir parámetros en el template de la skill
        if (step.getSkillParametersJson() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> params = objectMapper.readValue(
                    step.getSkillParametersJson(), Map.class);

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    String value = entry.getValue();
                    // Valor especial: usar el output del paso anterior
                    if ("__PREVIOUS_OUTPUT__".equals(value)) {
                        value = previousOutput;
                    }
                    template = template.replace("{{" + entry.getKey() + "}}", value);
                }
            } catch (Exception e) {
                log.warn("Error parsing skill parameters JSON: {}", e.getMessage());
            }
        } else {
            // Sin parámetros explícitos: inyectar previousOutput en el primer {{PARAM}}
            template = template.replaceFirst("\\{\\{[A-Z_]+\\}\\}", previousOutput);
        }

        return template;
    }

    private String buildTransformPrompt(TransformType transformType, String previousOutput) {
        return switch (transformType) {
            case SUMMARIZE ->
                "Resume el siguiente texto de forma concisa, manteniendo los puntos más importantes:\n\n" + previousOutput;

            case TRANSLATE_ES ->
                "Traduce el siguiente texto al español, manteniendo el tono y formato original:\n\n" + previousOutput;

            case TRANSLATE_EN ->
                "Translate the following text to English, maintaining the original tone and format:\n\n" + previousOutput;

            case FORMAT_MARKDOWN ->
                "Reformatea el siguiente contenido como Markdown bien estructurado con encabezados, " +
                "listas y énfasis donde corresponda. No cambies el contenido, solo el formato:\n\n" + previousOutput;

            case EXTRACT_KEYWORDS ->
                "Extrae y lista los conceptos clave, términos técnicos y temas principales del siguiente texto. " +
                "Proporciona una breve explicación de cada uno:\n\n" + previousOutput;
        };
    }
}
