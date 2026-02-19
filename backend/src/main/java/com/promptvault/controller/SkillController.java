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
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String tag // Added tag search parameter
    ) {
        Page<SkillDTO> skills;

        if (category != null && !category.isBlank()) {
            skills = skillService.getSkillsByCategory(category, pageable);
        } else if (tag != null && !tag.isBlank()) { // Handle tag search
            skills = skillService.getSkillsByTag(tag, pageable);
        }
        else {
            skills = skillService.getAllSkills(pageable);
        }
        
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
