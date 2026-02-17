package com.promptvault.service;

import com.promptvault.dto.AIImprovementResponse;
import com.promptvault.exception.ResourceNotFoundException;
import com.promptvault.model.Prompt;
import com.promptvault.repository.PromptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList; // AÑADIDO
import java.util.Arrays;
import java.util.List;

/**
 * Servicio para mejorar prompts usando IA (Groq).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIEnhancementService {
    
    private final GroqClient groqClient;
    private final PromptRepository promptRepository;
    
    /**
     * Mejora un prompt existente usando Groq API.
     * 
     * @param promptId ID del prompt a mejorar
     * @return respuesta con el prompt mejorado y explicación
     */
    public AIImprovementResponse improvePrompt(Long promptId) {
        log.info("Mejorando prompt con ID: {}", promptId);
        
        // Obtener prompt
        Prompt prompt = promptRepository.findById(promptId)
            .orElseThrow(() -> new ResourceNotFoundException("Prompt", "id", promptId));
        
        // Construir prompt de mejora
        String improvementPrompt = buildImprovementPrompt(prompt.getContent(), prompt.getCategory());
        
        try {
            // Llamar a Groq
            String groqResponse = groqClient.generateContent(improvementPrompt);
            
            // Parsear respuesta
            return parseImprovementResponse(prompt.getContent(), groqResponse);
            
        } catch (IOException e) {
            log.error("Error llamando a Groq API", e);
            throw new RuntimeException("Error mejorando prompt con Groq: " + e.getMessage(), e);
        }
    }
    
    /**
     * Construye el prompt que se enviará a Groq para mejorar el prompt original.
     * 
     * Aplica estructura profesional de 5 secciones:
     * - ROL: Perspectiva desde la que debe responder
     * - TAREA: Acción específica a realizar
     * - AUDIENCIA: Destinatario del resultado
     * - FORMATO: Cómo presentar la salida
     * - CONTEXTO: Información relevante adicional
     */
    private String buildImprovementPrompt(String originalContent, String category) {
        // This text block was already updated in ISSUE 24
        return String.format("""
            Eres un experto en prompt engineering con años de experiencia optimizando prompts para LLMs.
            
            Tu tarea es transformar el siguiente prompt en uno profesional usando la estructura de 5 secciones.
            
            PROMPT ORIGINAL (categoría: %s):
            ```
            %s
            ```
            
            ESTRUCTURA REQUERIDA:
            Transforma este prompt aplicando estas 5 secciones obligatorias:
            
            [ROL]
            Define quién debe responder o desde qué perspectiva:
            - Si es código: "Actúa como desarrollador senior especializado en..."
            - Si es debugging: "Actúa como experto en debugging de..."
            - Si es documentación: "Actúa como technical writer..."
            - Adapta el rol según la categoría y contenido
            
            [TAREA]
            Explica de forma clara y específica qué debe hacer el modelo:
            - Usa verbos de acción precisos
            - Divide tareas complejas en pasos numerados
            - Sé específico sobre el resultado esperado
            
            [AUDIENCIA]
            Indica para quién es el resultado:
            - Desarrolladores junior/senior
            - Usuarios técnicos/no técnicos
            - Stakeholders
            - Adapta según el contexto del prompt
            
            [FORMATO]
            Define cómo presentar la salida (elige el más apropiado):
            - Código comentado
            - Lista de pasos
            - Tabla comparativa
            - Markdown estructurado
            - JSON
            - Diagrama textual
            - Explicación narrativa
            IMPORTANTE: Elige TÚ el formato más útil según la tarea
            
            [CONTEXTO/DETALLES]
            Incluye información relevante:
            - Tecnologías específicas (versiones, frameworks)
            - Restricciones o limitaciones
            - Ejemplos si son necesarios
            - Mejores prácticas aplicables
            
            REGLAS ADICIONALES:
            1. Cada sección debe estar claramente marcada con su nombre entre corchetes: [ROL], [TAREA], etc.
            2. Usa un lenguaje preciso y sin ambigüedades
            3. Si el prompt original ya es bueno en algún aspecto, consérvalo
            4. Añade detalles técnicos relevantes según la categoría
            5. El prompt mejorado debe ser actionable (que se pueda ejecutar directamente)
            
            FORMATO DE RESPUESTA:
            Devuelve SOLO dos bloques separados por "---MEJORAS---":
            
            BLOQUE 1 - El prompt mejorado con las 5 secciones (sin introducción ni explicación)
            ---MEJORAS---
            BLOQUE 2 - Lista de mejoras realizadas (una por línea, empezando con "- ")
            
            EJEMPLO DE FORMATO:
            
            [ROL]
            Actúa como desarrollador senior...
            
            [TAREA]
            1. Analiza el código...
            2. Identifica el problema...
            
            [AUDIENCIA]
            Este análisis es para desarrolladores junior...
            
            [FORMATO]
            - Código corregido con comentarios explicativos
            - Explicación paso a paso de la solución
            
            [CONTEXTO/DETALLES]
            - Framework: Spring Boot 3.2
            - Java 17
            - Mejores prácticas: usar constructor injection...
            
            IMPORTANTE: No añadas ningún texto antes del prompt mejorado ni después de la lista de mejoras.
            Empieza directamente con [ROL] y termina con la última mejora.
            """, category, originalContent);
    }
    
    /**
     * Parsea la respuesta de Groq y extrae el prompt mejorado y las mejoras.
     * Valida que el prompt mejorado contenga las 5 secciones requeridas.
     */
    private AIImprovementResponse parseImprovementResponse(String originalContent, String groqResponse) {
        // Dividir respuesta en prompt mejorado y lista de mejoras
        String[] parts = groqResponse.split("---MEJORAS---");
        
        String improvedContent;
        List<String> improvements;
        
        if (parts.length >= 2) {
            improvedContent = parts[0].trim();
            
            // Parsear mejoras (líneas que empiezan con "- ")
            improvements = Arrays.stream(parts[1].split("\n"))
                .map(String::trim)
                .filter(line -> line.startsWith("-"))
                .map(line -> line.substring(1).trim())
                .toList();
        } else {
            // Si no encuentra el separador, usar toda la respuesta como mejora
            improvedContent = groqResponse.trim();
            improvements = List.of("Prompt mejorado con estructura profesional");
        }
        
        // Validar presencia de cada sección
        boolean hasRol = improvedContent.contains("[ROL]");
        boolean hasTarea = improvedContent.contains("[TAREA]");
        boolean hasAudiencia = improvedContent.contains("[AUDIENCIA]");
        boolean hasFormato = improvedContent.contains("[FORMATO]");
        boolean hasContexto = improvedContent.contains("[CONTEXTO");  // Acepta [CONTEXTO] o [CONTEXTO/DETALLES]
        
        // Calcular completitud (porcentaje de secciones presentes)
        int sectionsPresent = 0;
        if (hasRol) sectionsPresent++;
        if (hasTarea) sectionsPresent++;
        if (hasAudiencia) sectionsPresent++;
        if (hasFormato) sectionsPresent++;
        if (hasContexto) sectionsPresent++;
        
        int completeness = (sectionsPresent * 100) / 5;
        
        // Determinar calidad
        String quality;
        if (completeness == 100) {
            quality = "Excelente";
        } else if (completeness >= 80) {
            quality = "Buena";
        } else if (completeness >= 60) {
            quality = "Mejorable";
        } else {
            quality = "Incompleta";
        }
        
        // Crear objeto de validación
        AIImprovementResponse.StructureValidation validation = 
            AIImprovementResponse.StructureValidation.builder()
                .hasRol(hasRol)
                .hasTarea(hasTarea)
                .hasAudiencia(hasAudiencia)
                .hasFormato(hasFormato)
                .hasContexto(hasContexto)
                .completeness(completeness)
                .quality(quality)
                .build();
        
        // Log de validación
        if (completeness == 100) {
            log.info("✓ Prompt mejorado con estructura completa (100%): ROL, TAREA, AUDIENCIA, FORMATO, CONTEXTO");
        } else {
            log.warn("⚠ Estructura parcial ({}%): Faltan secciones", completeness);
            if (!hasRol) log.warn("  - Falta [ROL]");
            if (!hasTarea) log.warn("  - Falta [TAREA]");
            if (!hasAudiencia) log.warn("  - Falta [AUDIENCIA]");
            if (!hasFormato) log.warn("  - Falta [FORMATO]");
            if (!hasContexto) log.warn("  - Falta [CONTEXTO]");
        }
        
        // Si faltan secciones, añadir advertencia
        if (completeness < 100) {
            String note = String.format("""
                ⚠️ ADVERTENCIA: Este prompt mejorado tiene una completitud del %d%%
                Secciones faltantes pueden afectar la efectividad del prompt.
                Se recomienda revisar y completar manualmente las secciones faltantes.
                
                """, completeness);
            improvedContent = note + improvedContent;
            
            // Añadir info en mejoras
            List<String> updatedImprovements = new ArrayList<>(improvements);
            updatedImprovements.add(0, 
                String.format("⚠️ Estructura %s (%d%% completa) - revisar secciones faltantes", 
                    quality, completeness));
            improvements = updatedImprovements;
        }
        
        return AIImprovementResponse.builder()
            .originalContent(originalContent)
            .improvedContent(improvedContent)
            .improvements(improvements)
            .structureValidation(validation) // AÑADIDO
            .tokenUsage(null)
            .build();
    }
}
