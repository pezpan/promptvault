package com.promptvault.service;

import com.promptvault.dto.AIImprovementResponse;
import com.promptvault.exception.ResourceNotFoundException;
import com.promptvault.model.Prompt;
import com.promptvault.repository.PromptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Servicio para mejorar prompts usando IA (Groq API).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIEnhancementService {
    
    private final GroqClient groqClient;  // CAMBIADO
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
            String groqResponse = groqClient.generateContent(improvementPrompt);  // CAMBIADO
            
            // Parsear respuesta
            return parseImprovementResponse(prompt.getContent(), groqResponse);  // CAMBIADO
            
        } catch (IOException e) {
            log.error("Error llamando a Groq API", e);  // CAMBIADO
            throw new RuntimeException("Error mejorando prompt con Groq: " + e.getMessage(), e);  // CAMBIADO
        }
    }
    
    /**
     * Construye el prompt que se enviará a Groq para mejorar el prompt original.
     */
    private String buildImprovementPrompt(String originalContent, String category) {
        return String.format("""
            Eres un experto en prompt engineering.
            
            Tengo este prompt de categoría "%s":
            
            ```
            %s
            ```
            
            TAREA:
            Mejora este prompt siguiendo estas reglas:
            
            1. ESTRUCTURA CLARA:
               - Añade secciones: OBJETIVO, CONTEXTO, REQUISITOS, OUTPUT ESPERADO
               - Usa encabezados y listas
            
            2. ESPECIFICIDAD:
               - Sé más específico en las instrucciones
               - Añade detalles técnicos relevantes
               - Define claramente el formato de salida
            
            3. CLARIDAD:
               - Usa lenguaje preciso y sin ambigüedades
               - Divide tareas complejas en pasos
            
            4. CONTEXTO:
               - Añade información de contexto relevante
               - Especifica frameworks/tecnologías si aplica
            
            FORMATO DE RESPUESTA:
            Devuelve SOLO dos secciones separadas por "---MEJORAS---":
            
            1. El prompt mejorado (sin explicaciones adicionales)
            2. Una lista de mejoras realizadas (una por línea, empezando con "- ")
            
            Ejemplo:
            [Prompt mejorado aquí]
            ---MEJORAS---
            - Añadida sección de CONTEXTO con especificaciones técnicas
            - Estructuradas las instrucciones en pasos numerados
            - Definido formato de salida esperado
            """, category, originalContent);
    }
    
    /**
     * Parsea la respuesta de Groq y extrae el prompt mejorado y las mejoras.
     */
    private AIImprovementResponse parseImprovementResponse(String originalContent, String groqResponse) {  // CAMBIADO nombre parámetro
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
            improvements = List.of("Prompt mejorado por IA");
        }
        
        return AIImprovementResponse.builder()
            .originalContent(originalContent)
            .improvedContent(improvedContent)
            .improvements(improvements)
            .tokenUsage(null) // Por ahora no calculamos tokens
            .build();
    }
}