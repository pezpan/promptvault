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
 * Cliente para Groq API.
 * 
 * Groq ofrece acceso gratuito a modelos LLM de alta calidad
 * con límites generosos (30 req/min, 14,400/día).
 * 
 * API Key: https://console.groq.com/keys
 */
@Service
@Slf4j
public class GroqClient {
    
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    // Modelos disponibles en Groq:
    // - llama-3.3-70b-versatile (recomendado)
    // - llama-3.1-8b-instant (más rápido)
    // - mixtral-8x7b-32768
    private static final String MODEL = "llama-3.3-70b-versatile";
    
    @Value("${groq.api.key:}")
    private String apiKey;
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public GroqClient() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Genera contenido usando Groq API.
     * 
     * @param prompt el prompt a enviar a Groq
     * @return el texto generado por Groq
     * @throws IOException si hay error en la comunicación
     */
    public String generateContent(String prompt) throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "GROQ_API_KEY no está configurada. " +
                "Obtén una API key gratis en: https://console.groq.com/keys"
            );
        }
        
        String requestBody = buildRequestBody(prompt);
        
        Request request = new Request.Builder()
            .url(GROQ_API_URL)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .post(RequestBody.create(requestBody, JSON))
            .build();
        
        log.debug("Llamando a Groq API (modelo: {})...", MODEL);
        
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            
            if (!response.isSuccessful()) {
                log.error("Error en Groq API: {} - {}", response.code(), responseBody);
                throw new IOException("Error de Groq API: " + response.code() + " - " + responseBody);
            }
            
            return parseResponse(responseBody);
        }
    }
    
    /**
     * Construye el cuerpo de la petición para Groq.
     * Groq usa formato compatible con OpenAI API.
     */
    private String buildRequestBody(String prompt) {
        String escapedPrompt = escapeJson(prompt);
        return "{\n" +
               "  \"model\": \"" + MODEL + "\",\n" +
               "  \"messages\": [\n" +
               "    {\n" +
               "      \"role\": \"user\",\n" +
               "      \"content\": \"" + escapedPrompt + "\"\n" +
               "    }\n" +
               "  ],\n" +
               "  \"temperature\": 0.2,\n" +
               "  \"max_tokens\": 2048,\n" +
               "  \"top_p\": 0.9\n" +
               "}";
    }
    
    /**
     * Parsea la respuesta de Groq y extrae el texto generado.
     */
    private String parseResponse(String responseBody) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                String text = firstChoice.path("message").path("content").asText();
                
                if (text.isBlank()) {
                    throw new IOException("Respuesta vacía de Groq");
                }
                
                log.debug("Respuesta de Groq recibida: {} caracteres", text.length());
                return text;
            }
            
            log.error("Respuesta de Groq en formato inesperado: {}", responseBody);
            throw new IOException("Respuesta de Groq en formato inesperado");
            
        } catch (Exception e) {
            log.error("Error parseando respuesta de Groq", e);
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
