# ISSUE 17: GeminiClient - Integración con Gemini API

Crea el cliente para comunicarse con la API de Gemini.

## Archivos a Generar

### 1. GeminiClient.java

**Ruta**: `backend/src/main/java/com/promptvault/service/GeminiClient.java`

**Contenido**:

```java
package com.promptvault.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Cliente para comunicarse con la API de Google Gemini.
 */
@Service
@Slf4j
public class GeminiClient {
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    @Value("${gemini.api.key:}")
    private String apiKey;
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public GeminiClient() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Genera contenido usando Gemini API.
     * 
     * @param prompt el prompt a enviar a Gemini
     * @return el texto generado por Gemini
     * @throws IOException si hay error en la comunicación
     */
    public String generateContent(String prompt) throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY no está configurada. Añádela en application.yml");
        }
        
        String requestBody = buildRequestBody(prompt);
        
        Request request = new Request.Builder()
            .url(GEMINI_API_URL + "?key=" + apiKey)
            .post(RequestBody.create(requestBody, JSON))
            .build();
        
        log.debug("Llamando a Gemini API...");
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Sin body";
                log.error("Error en Gemini API: {} - {}", response.code(), errorBody);
                throw new IOException("Error de Gemini API: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            return parseResponse(responseBody);
        }
    }
    
    /**
     * Construye el cuerpo de la petición para Gemini.
     */
    private String buildRequestBody(String prompt) {
        return String.format("""
            {
              "contents": [{
                "parts": [{"text": "%s"}]
              }],
              "generationConfig": {
                "temperature": 0.2,
                "topK": 40,
                "topP": 0.8,
                "maxOutputTokens": 2048
              }
            }
            """, escapeJson(prompt));
    }
    
    /**
     * Parsea la respuesta de Gemini y extrae el texto generado.
     */
    private String parseResponse(String responseBody) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    log.debug("Respuesta de Gemini recibida: {} caracteres", text.length());
                    return text;
                }
            }
            
            log.error("Respuesta de Gemini en formato inesperado: {}", responseBody);
            throw new IOException("Respuesta de Gemini en formato inesperado");
            
        } catch (Exception e) {
            log.error("Error parseando respuesta de Gemini", e);
            throw new IOException("Error parseando respuesta: " + e.getMessage(), e);
        }
    }
    
    /**
     * Escapa caracteres especiales para JSON.
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
```

### 2. Añadir configuración en application.yml

**Archivo**: `backend/src/main/resources/application.yml`

**Añadir al final**:

```yaml
# Gemini API
gemini:
  api:
    key: ${GEMINI_API_KEY:}
```

### 3. Añadir variable de entorno

En tu sistema, configura:

```bash
# Windows PowerShell
$env:GEMINI_API_KEY = "tu_api_key_de_gemini"

# O añádelo en application-dev.yml:
gemini:
  api:
    key: AIzaSy...tu_key_aqui
```

## Verificación

```bash
mvn clean compile
# Debe compilar sin errores

# Probar manualmente (opcional)
# Crear un test o usar desde otro servicio
```

## Notas

- La API key puede venir de variable de entorno `GEMINI_API_KEY` o de `application.yml`
- Timeout configurado a 30 segundos
- Temperature en 0.2 para respuestas consistentes
- Max 2048 tokens de salida
