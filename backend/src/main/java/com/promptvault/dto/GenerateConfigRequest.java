package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateConfigRequest {
    private List<Long> serverIds;
    private Map<String, Map<String, String>> envVars;  // serverName -> {KEY: value}
}
