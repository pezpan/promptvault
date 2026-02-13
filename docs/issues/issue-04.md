### ISSUE 04: PromptVaultApplication.java (Main Class)

**Comando**: `Implementa ISSUE 04`

**Archivo**: `backend/src/main/java/com/promptvault/PromptVaultApplication.java`

**Contenido**:
```java
package com.promptvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de PromptVault.
 * 
 * PromptVault es una plataforma REST API para gestionar prompts de IA
 * con funcionalidades de mejora automática usando Gemini API.
 */
@SpringBootApplication
public class PromptVaultApplication {
    
    /**
     * Método principal que inicia la aplicación Spring Boot.
     * 
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(PromptVaultApplication.class, args);
    }
}
```

**Verificar**: `mvn clean compile` debe compilar sin errores
