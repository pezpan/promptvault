package com.promptvault.service;

import com.promptvault.dto.MCPTestRequest.TestLevel;
import com.promptvault.dto.MCPTestResult;
import com.promptvault.dto.MCPTestResult.ConnectivityResult;
import com.promptvault.dto.MCPTestResult.ValidationIssue;
import com.promptvault.exception.ResourceNotFoundException;
import com.promptvault.model.MCPServer;
import com.promptvault.repository.MCPServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class MCPTesterService {

    private final MCPServerRepository mcpServerRepository;
    private final MCPServerService mcpServerService;
    private final MCPValidatorService validatorService;
    private final MCPConnectivityService connectivityService;

    /**
     * Test de un servidor almacenado en BD por ID
     */
    public MCPTestResult testById(Long serverId, TestLevel level) {
        MCPServer server = mcpServerRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("MCPServer", "id", serverId));

        // Generar la config JSON del servidor para validarla usando el servicio existente
        String configJson = mcpServerService.generateConfig(Collections.singletonList(serverId), Collections.emptyMap());

        return runTest(serverId, server.getName(), configJson, level, server);
    }

    /**
     * Test de una configuración JSON directa (sin necesidad de estar en BD)
     */
    public MCPTestResult testConfigJson(String configJson, TestLevel level) {
        return runTest(null, "Custom Config", configJson, level, null);
    }

    /**
     * Test de todos los servidores almacenados (batch)
     */
    public List<MCPTestResult> testAll(TestLevel level) {
        return mcpServerRepository.findAll().stream()
                .map(server -> testById(server.getId(), level))
                .toList();
    }

    private MCPTestResult runTest(Long serverId, String serverName,
                                   String configJson, TestLevel level, MCPServer server) {
        log.info("Testing MCP server: {} (level: {})", serverName, level);

        // 1. Validación estática siempre se ejecuta
        List<ValidationIssue> issues = validatorService.validateConfigJson(configJson);
        MCPTestResult.TestStatus status = validatorService.determineStatus(issues);

        // 2. Conectividad solo si se solicita y la config es válida
        ConnectivityResult connectivity = null;
        if (level == TestLevel.CONNECTIVITY) {
            if (status != MCPTestResult.TestStatus.ERROR) {
                connectivity = server != null
                        ? connectivityService.testConnectivity(server)
                        : ConnectivityResult.builder()
                                .attempted(false)
                                .reachable(false)
                                .error("Test de conectividad no disponible para configs sin servidor registrado")
                                .build();

                // Si hay conectividad fallida, actualizar status
                if (connectivity != null && connectivity.isAttempted() && !connectivity.isReachable()) {
                    status = MCPTestResult.TestStatus.UNREACHABLE;
                }
            } else {
                connectivity = ConnectivityResult.builder()
                        .attempted(false)
                        .reachable(false)
                        .error("Test de conectividad omitido: la configuración tiene errores")
                        .build();
            }
        }

        boolean isValid = status == MCPTestResult.TestStatus.OK
                       || status == MCPTestResult.TestStatus.WARNING;

        return MCPTestResult.builder()
                .serverId(serverId)
                .serverName(serverName)
                .valid(isValid)
                .status(status)
                .issues(issues)
                .connectivity(connectivity)
                .testedAt(LocalDateTime.now())
                .build();
    }
}
