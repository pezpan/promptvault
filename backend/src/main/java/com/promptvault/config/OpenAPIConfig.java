package com.promptvault.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI/Swagger para documentación de la API.
 */
@Configuration
public class OpenAPIConfig {
    
    /**
     * Configura la información de la API que aparecerá en Swagger UI.
     * 
     * @return instancia configurada de OpenAPI
     */
    @Bean
    public OpenAPI promptVaultOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("PromptVault API")
                .description("REST API para gestión y mejora de prompts con IA")
                .version("1.0.0")
                .contact(new Contact()
                    .name("PromptVault Team")
                    .url("https://github.com/promptvault")));
    }
}