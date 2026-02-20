package com.promptvault.controller;

import com.promptvault.dto.GeneratePromptRequest;
import com.promptvault.dto.SkillBuildRequest;
import com.promptvault.dto.SkillBuildResult;
import com.promptvault.dto.SkillDTO;
import com.promptvault.service.SkillBuilderService;
import com.promptvault.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    private final SkillBuilderService skillBuilderService;
    
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

    @PostMapping("/build")
    @Operation(
        summary = "Generar una nueva Skill con IA",
        description = """
            Usa Groq IA para generar una Skill completa a partir de una descripción en
            lenguaje natural. La IA crea automáticamente:
            - El template parametrizable con {{PARAMETROS}}
            - Los parámetros detectados y sus descripciones
            - Un ejemplo de output
            - Una puntuación estimada de calidad (0-100)
            
            **Ejemplo de request:**
            ```json
            {
              "objective": "Quiero una skill para revisar código Python buscando vulnerabilidades de seguridad OWASP Top 10",
              "targetAudience": "Desarrolladores Python con conocimientos de seguridad",
              "exampleInputs": ["def login(user, pwd): return db.query(f'SELECT * FROM users WHERE pwd={pwd}')"],
              "desiredOutputFormat": "Lista de vulnerabilidades con: severidad, CWE ID, descripción y código corregido",
              "category": "security",
              "saveToDatabase": false
            }
            ```
            
            **Nota:** Con `saveToDatabase: true` la skill se guarda directamente en BD
            y puede usarse inmediatamente con `POST /api/skills/{id}/generate`.
            """
    )
    public ResponseEntity<SkillBuildResult> buildSkill(
            @Valid @RequestBody SkillBuildRequest request) {
        return ResponseEntity.ok(skillBuilderService.buildSkill(request));
    }
}
