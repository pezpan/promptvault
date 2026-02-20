package com.promptvault.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptvault.dto.MCPTestResult;
import com.promptvault.dto.MCPTestResult.ValidationIssue;
import com.promptvault.dto.MCPTestResult.ValidationIssue.Severity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MCPValidatorService {

    private final ObjectMapper objectMapper;

    /**
     * Valida la estructura JSON de una configuración MCP.
     * Formato esperado del JSON generado por PromptVault:
     * {
     *   "mcpServers": {
     *     "serverName": {
     *       "command": "npx",
     *       "args": ["-y", "@modelcontextprotocol/server-github"],
     *       "env": { "GITHUB_TOKEN": "your_token" }
     *     }
     *   }
     * }
     */
    public List<ValidationIssue> validateConfigJson(String configJson) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (configJson == null || configJson.isBlank()) {
            issues.add(ValidationIssue.builder()
                    .severity(Severity.ERROR)
                    .field("configJson")
                    .message("La configuración está vacía")
                    .suggestion("Usa el endpoint POST /api/mcp-servers/generate-config para generar la configuración")
                    .build());
            return issues;
        }

        // 1. Validar que es JSON válido
        JsonNode root;
        try {
            root = objectMapper.readTree(configJson);
        } catch (Exception e) {
            issues.add(ValidationIssue.builder()
                    .severity(Severity.ERROR)
                    .field("configJson")
                    .message("JSON malformado: " + e.getMessage())
                    .suggestion("Verifica que el JSON tiene llaves y comillas correctamente cerradas")
                    .build());
            return issues;
        }

        // 2. Validar estructura raíz: debe tener "mcpServers"
        if (!root.has("mcpServers")) {
            issues.add(ValidationIssue.builder()
                    .severity(Severity.ERROR)
                    .field("mcpServers")
                    .message("Falta el campo raíz 'mcpServers'")
                    .suggestion("La estructura correcta es: { \"mcpServers\": { \"nombre\": { ... } } }")
                    .build());
            return issues;
        }

        JsonNode mcpServers = root.get("mcpServers");

        if (!mcpServers.isObject() || mcpServers.isEmpty()) {
            issues.add(ValidationIssue.builder()
                    .severity(Severity.WARNING)
                    .field("mcpServers")
                    .message("El objeto mcpServers está vacío")
                    .suggestion("Añade al menos un servidor MCP")
                    .build());
            return issues;
        }

        // 3. Validar cada servidor
        mcpServers.fields().forEachRemaining(entry -> {
            String serverName = entry.getKey();
            JsonNode serverConfig = entry.getValue();
            issues.addAll(validateSingleServer(serverName, serverConfig));
        });

        return issues;
    }

    private List<ValidationIssue> validateSingleServer(String name, JsonNode config) {
        List<ValidationIssue> issues = new ArrayList<>();
        String prefix = "mcpServers." + name;

        // Campo "command" requerido
        if (!config.has("command") || config.get("command").asText().isBlank()) {
            issues.add(ValidationIssue.builder()
                    .severity(Severity.ERROR)
                    .field(prefix + ".command")
                    .message("Falta el campo 'command' en el servidor '" + name + "'")
                    .suggestion("Ejemplo: \"command\": \"npx\" o \"command\": \"uvx\"")
                    .build());
        } else {
            String command = config.get("command").asText();
            // Comandos conocidos válidos
            List<String> validCommands = List.of("npx", "uvx", "node", "python", "python3", "docker");
            if (validCommands.stream().noneMatch(command::equalsIgnoreCase)) {
                issues.add(ValidationIssue.builder()
                        .severity(Severity.WARNING)
                        .field(prefix + ".command")
                        .message("Comando desconocido: '" + command + "'")
                        .suggestion("Los comandos más comunes son: npx, uvx, node, python, docker")
                        .build());
            }
        }

        // Campo "args" recomendado
        if (!config.has("args")) {
            issues.add(ValidationIssue.builder()
                    .severity(Severity.WARNING)
                    .field(prefix + ".args")
                    .message("No se especificaron argumentos 'args' para '" + name + "'")
                    .suggestion("Ejemplo: \"args\": [\"-y\", \"@modelcontextprotocol/server-" + name + "\"]")
                    .build());
        } else if (!config.get("args").isArray()) {
            issues.add(ValidationIssue.builder()
                    .severity(Severity.ERROR)
                    .field(prefix + ".args")
                    .message("'args' debe ser un array, no un string")
                    .suggestion("Cambia a: \"args\": [\"" + config.get("args").asText() + "\"]")
                    .build());
        }

        // Variables de entorno - detectar placeholders sin rellenar
        if (config.has("env")) {
            JsonNode env = config.get("env");
            env.fields().forEachRemaining(envEntry -> {
                String value = envEntry.getValue().asText();
                if (value.contains("your_") || value.contains("YOUR_") ||
                        value.equals("") || value.equals("null") ||
                        value.startsWith("<") && value.endsWith(">")) {
                    issues.add(ValidationIssue.builder()
                            .severity(Severity.WARNING)
                            .field(prefix + ".env." + envEntry.getKey())
                            .message("La variable de entorno '" + envEntry.getKey() + "' parece un placeholder sin rellenar: '" + value + "'")
                            .suggestion("Reemplaza '" + value + "' con tu valor real de " + envEntry.getKey())
                            .build());
                }
            });
        }

        // INFO: confirmar que la config parece completa
        if (issues.isEmpty()) {
            issues.add(ValidationIssue.builder()
                    .severity(Severity.INFO)
                    .field(prefix)
                    .message("Configuración del servidor '" + name + "' es válida")
                    .suggestion("Recuerda añadir este bloque a tu claude_desktop_config.json o .vscode/mcp.json")
                    .build());
        }

        return issues;
    }

    /**
     * Determina el status general basándose en los issues encontrados
     */
    public MCPTestResult.TestStatus determineStatus(List<ValidationIssue> issues) {
        boolean hasErrors = issues.stream().anyMatch(i -> i.getSeverity() == Severity.ERROR);
        boolean hasWarnings = issues.stream().anyMatch(i -> i.getSeverity() == Severity.WARNING);

        if (hasErrors) return MCPTestResult.TestStatus.ERROR;
        if (hasWarnings) return MCPTestResult.TestStatus.WARNING;
        return MCPTestResult.TestStatus.OK;
    }
}
