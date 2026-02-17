package com.promptvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de PromptVault.
 * 
 * PromptVault es una plataforma REST API para gestionar prompts de IA
 * con funcionalidades de mejora automática usando Groq API.
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