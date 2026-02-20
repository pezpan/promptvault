package com.promptvault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillBuildRequest {

    @NotBlank(message = "La descripción del objetivo es obligatoria")
    @Size(min = 20, max = 500, message = "La descripción debe tener entre 20 y 500 caracteres")
    private String objective;
    // Ejemplo: "Quiero una skill para revisar código Python buscando vulnerabilidades de seguridad"

    @Size(max = 100)
    private String targetAudience;
    // Ejemplo: "Desarrolladores Python con conocimientos de seguridad"

    private List<String> exampleInputs;
    // Ejemplos de inputs que recibirá la skill
    // Ejemplo: ["def login(user, pwd): return db.query(f'SELECT * FROM users WHERE pwd={pwd}')"]

    private String desiredOutputFormat;
    // Ejemplo: "Lista de vulnerabilidades con severidad, descripción y corrección sugerida"

    private String category;
    // Categoría deseada: "development", "security", "writing", etc.

    @Builder.Default
    private boolean saveToDatabase = false;
    // Si true, guarda la skill generada directamente en BD
}
