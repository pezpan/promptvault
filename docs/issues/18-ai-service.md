# ISSUE 18: AIEnhancementService - Servicio de Mejora con IA

Crea el servicio que usa Gemini para mejorar prompts.

## Archivos a Generar

### 1. AIImprovementResponse.java (DTO)

**Ruta**: `backend/src/main/java/com/promptvault/dto/AIImprovementResponse.java`

**Contenido**:

```java
package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que contiene el resultado de mejorar un prompt con IA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIImprovementResponse {
    
    /**
     * Contenido original del prompt.
     */
    private String originalContent;
    
    /**
     * Contenido mejorado por la IA.
     */
    private String improvedContent;
    
    /**
     * Lista de mejoras realizadas (explicación).
     */
    private List<String> improvements;
    
    /**
     * Tokens utilizados en la petición (opcional).
     */
    private TokenUsage tokenUsage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
    }
}
```

### 2. AIEnhancementService.java

**Ruta**: `backend/src/main/java/com/promptvault/service/AIEnhancementService.java`

**Contenido**:

```java
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
 * Servicio para mejorar prompts usando IA (Gemini).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIEnhancementService {
    
    private final GeminiClient geminiClient;
    private final PromptRepository promptRepository;
    
    /**
     * Mejora un prompt existente usando Gemini API.
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
            // Llamar a Gemini
            String geminiResponse = geminiClient.generateContent(improvementPrompt);
            
            // Parsear respuesta
            return parseImprovementResponse(prompt.getContent(), geminiResponse);
            
        } catch (IOException e) {
            log.error("Error llamando a Gemini API", e);
            throw new RuntimeException("Error mejorando prompt: " + e.getMessage(), e);
        }
    }
    
    /**
     * Construye el prompt que se enviará a Gemini para mejorar el prompt original.
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
     * Parsea la respuesta de Gemini y extrae el prompt mejorado y las mejoras.
     */
    private AIImprovementResponse parseImprovementResponse(String originalContent, String geminiResponse) {
        // Dividir respuesta en prompt mejorado y lista de mejoras
        String[] parts = geminiResponse.split("---MEJORAS---");
        
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
            improvedContent = geminiResponse.trim();
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
```

## Verificación

```bash
mvn clean compile
# Debe compilar sin errores

# Probar será en el siguiente issue (endpoint REST)
```

## Notas

- El servicio construye un prompt específico para que Gemini mejore prompts
- Parsea la respuesta usando un separador `---MEJORAS---`
- Si falla el parseo, retorna la respuesta completa
- Manejo de errores con RuntimeException (se captura en GlobalExceptionHandler)
