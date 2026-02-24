package com.promptvault.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecuteRequest {

    @NotBlank(message = "El input inicial es obligatorio")
    private String initialInput;
    // El texto que se pasa al primer paso del workflow
    // Ejemplo: el código fuente a analizar, el texto a procesar, etc.

    private String additionalContext;
    // Contexto adicional opcional que se añade a TODOS los pasos
    // Útil para dar información sobre el proyecto, lenguaje, etc.
}
