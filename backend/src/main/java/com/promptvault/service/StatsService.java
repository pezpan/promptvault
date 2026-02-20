package com.promptvault.service;

import com.promptvault.dto.*;
import com.promptvault.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final PromptRepository promptRepository;
    private final PromptImprovementRepository improvementRepository;
    private final MCPServerRepository mcpServerRepository;
    private final SkillRepository skillRepository;
    private final CategoryRepository categoryRepository; // This repository was not requested, but it is in the provided code.

    public DashboardDTO getDashboard() {
        return DashboardDTO.builder()
                .global(getGlobalStats())
                .prompts(getPromptStats())
                .ai(getAIStats())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    public GlobalStatsDTO getGlobalStats() {
        long totalPrompts = promptRepository.count();
        long totalImprovements = improvementRepository.count();
        long totalMcpServers = mcpServerRepository.count();
        long totalSkills = skillRepository.count();
        long improvedPrompts = promptRepository.countImprovedPrompts();

        double improvementRatio = totalPrompts > 0
                ? Math.round((double) improvedPrompts / totalPrompts * 100.0) / 100.0
                : 0.0;

        return GlobalStatsDTO.builder()
                .totalPrompts(totalPrompts)
                .totalImprovements(totalImprovements)
                .totalMcpServers(totalMcpServers)
                .totalSkills(totalSkills)
                .improvementRatio(improvementRatio)
                .build();
    }

    public PromptStatsDTO getPromptStats() {
        List<Object[]> raw = promptRepository.countByCategory();
        Map<String, Long> byCategory = raw.stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (Long) r[1]
                ));

        long improved = promptRepository.countImprovedPrompts();
        long total = promptRepository.count();

        return PromptStatsDTO.builder()
                .promptsByCategory(byCategory)
                .improvedPrompts(improved)
                .notImprovedPrompts(total - improved)
                .build();
    }

    public AIStatsDTO getAIStats() {
        String mostUsedSkill = skillRepository
                .findTopByOrderByUsageCountDesc()
                .map(s -> s.getName())
                .orElse("N/A");

        String mostPopularMcp = mcpServerRepository
                .findTopByOrderByUsageCountDesc()
                .map(m -> m.getName())
                .orElse("N/A");

        long totalAICalls = improvementRepository.count();

        List<RecentImprovementDTO> recent = improvementRepository
                .findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(i -> RecentImprovementDTO.builder()
                        .promptId(i.getPrompt().getId())
                        .promptTitle(i.getPrompt().getTitle())
                        .improvedAt(i.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return AIStatsDTO.builder()
                .mostUsedSkill(mostUsedSkill)
                .mostPopularMcpServer(mostPopularMcp)
                .totalAICallsMade(totalAICalls)
                .recentImprovements(recent)
                .build();
    }
}
