package com.promptvault.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptvault.dto.*;
import com.promptvault.exception.ResourceNotFoundException;
import com.promptvault.model.Workflow;
import com.promptvault.model.WorkflowStep;
import com.promptvault.repository.SkillRepository;
import com.promptvault.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final SkillRepository skillRepository;
    private final WorkflowExecutionEngine executionEngine;
    private final ObjectMapper objectMapper;

    // ─── CRUD ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<WorkflowDTO> findAll() {
        return workflowRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkflowDTO> findByCategory(String category) {
        return workflowRepository.findByCategory(category).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkflowDTO> findPopular() {
        return workflowRepository.findTop5ByOrderByUsageCountDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkflowDetailDTO findById(Long id) {
        Workflow workflow = getOrThrow(id);
        return toDetailDTO(workflow);
    }

    @Transactional
    public WorkflowDetailDTO create(WorkflowCreateRequest request) {
        Workflow workflow = new Workflow();
        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow.setCategory(request.getCategory());
        workflow.setInputDescription(request.getInputDescription());
        workflow.setOutputDescription(request.getOutputDescription());

        // Crear pasos en orden
        List<WorkflowStep> steps = buildSteps(request, workflow);
        workflow.setSteps(steps);

        return toDetailDTO(workflowRepository.save(workflow));
    }

    @Transactional
    public void delete(Long id) {
        if (!workflowRepository.existsById(id)) {
            throw new ResourceNotFoundException("Workflow", "id", id);
        }
        workflowRepository.deleteById(id);
    }

    // ─── EJECUCIÓN ──────────────────────────────────────────────────────

    @Transactional
    public WorkflowExecutionResult execute(Long id, WorkflowExecuteRequest request) {
        Workflow workflow = getOrThrow(id);

        // Incrementar contador de uso
        workflow.setUsageCount(workflow.getUsageCount() + 1);
        workflowRepository.save(workflow);

        log.info("Executing workflow '{}' ({} steps) with input length: {}",
                workflow.getName(), workflow.getSteps().size(), request.getInitialInput().length());

        return executionEngine.execute(
                workflow.getId(),
                workflow.getName(),
                workflow.getSteps(),
                request
        );
    }

    // ─── MAPPERS ────────────────────────────────────────────────────────

    private WorkflowDTO toDTO(Workflow w) {
        List<String> stepNames = w.getSteps().stream()
                .map(WorkflowStep::getName)
                .collect(Collectors.toList());

        return WorkflowDTO.builder()
                .id(w.getId())
                .name(w.getName())
                .description(w.getDescription())
                .category(w.getCategory())
                .inputDescription(w.getInputDescription())
                .outputDescription(w.getOutputDescription())
                .stepCount(w.getSteps().size())
                .usageCount(w.getUsageCount())
                .stepNames(stepNames)
                .build();
    }

    private WorkflowDetailDTO toDetailDTO(Workflow w) {
        List<WorkflowStepDTO> stepDTOs = w.getSteps().stream()
                .map(this::toStepDTO)
                .collect(Collectors.toList());

        return WorkflowDetailDTO.builder()
                .id(w.getId())
                .name(w.getName())
                .description(w.getDescription())
                .category(w.getCategory())
                .inputDescription(w.getInputDescription())
                .outputDescription(w.getOutputDescription())
                .usageCount(w.getUsageCount())
                .steps(stepDTOs)
                .build();
    }

    private WorkflowStepDTO toStepDTO(WorkflowStep step) {
        // Resolver nombre de skill si aplica
        String skillName = null;
        if (step.getSkillId() != null) {
            skillName = skillRepository.findById(step.getSkillId())
                    .map(s -> s.getName())
                    .orElse("Skill no encontrada (ID: " + step.getSkillId() + ")");
        }

        // Deserializar parámetros JSON a Map
        java.util.Map<String, String> skillParams = null;
        if (step.getSkillParametersJson() != null) {
            try {
                skillParams = objectMapper.readValue(step.getSkillParametersJson(),
                        new TypeReference<java.util.Map<String, String>>() {});
            } catch (JsonProcessingException e) {
                log.warn("Could not parse skillParametersJson for step {}", step.getId());
            }
        }

        return WorkflowStepDTO.builder()
                .id(step.getId())
                .stepOrder(step.getStepOrder())
                .name(step.getName())
                .stepType(step.getStepType())
                .skillId(step.getSkillId())
                .skillName(skillName)
                .skillParameters(skillParams)
                .promptTemplate(step.getPromptTemplate())
                .transformType(step.getTransformType())
                .systemInstruction(step.getSystemInstruction())
                .build();
    }

    private List<WorkflowStep> buildSteps(WorkflowCreateRequest request, Workflow workflow) {
        List<WorkflowStep> steps = new java.util.ArrayList<>();
        int order = 1;

        for (WorkflowCreateRequest.StepRequest stepReq : request.getSteps()) {
            WorkflowStep step = new WorkflowStep();
            step.setWorkflow(workflow);
            step.setStepOrder(order++);
            step.setName(stepReq.getName());
            step.setStepType(stepReq.getStepType());
            step.setSkillId(stepReq.getSkillId());
            step.setPromptTemplate(stepReq.getPromptTemplate());
            step.setTransformType(stepReq.getTransformType());
            step.setSystemInstruction(stepReq.getSystemInstruction());

            // Serializar parámetros de skill a JSON
            if (stepReq.getSkillParameters() != null) {
                try {
                    step.setSkillParametersJson(
                        objectMapper.writeValueAsString(stepReq.getSkillParameters()));
                } catch (JsonProcessingException e) {
                    log.warn("Could not serialize skill parameters for step: {}", stepReq.getName());
                }
            }

            steps.add(step);
        }
        return steps;
    }

    private Workflow getOrThrow(Long id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow", "id", id));
    }
}
