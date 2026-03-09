package com.promptvault.service;

import com.promptvault.model.ContextPack;
import com.promptvault.model.MCPServer;
import com.promptvault.model.Prompt;
import com.promptvault.model.Skill;
import com.promptvault.repository.ContextPackRepository;
import com.promptvault.repository.MCPServerRepository;
import com.promptvault.repository.PromptRepository;
import com.promptvault.repository.SkillRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextPackSeedService {

    private final ContextPackRepository contextPackRepository;
    private final PromptRepository promptRepository;
    private final SkillRepository skillRepository;
    private final MCPServerRepository mcpServerRepository;

    @Transactional
    public void seed() {
        if (contextPackRepository.count() > 0) {
            log.info("Context Packs already seeded.");
            return;
        }

        // Verificar si hay datos base (data.sql debería haber corrido)
        long promptCount = promptRepository.count();
        long skillCount = skillRepository.count();
        long mcpCount = mcpServerRepository.count();
        
        log.info("Current DB state for seeding: {} Prompts, {} Skills, {} MCPs", 
                 promptCount, skillCount, mcpCount);

        if (promptCount == 0 || skillCount == 0) {
            log.warn("Database is empty! Seeds might be empty if data.sql hasn't finished yet.");
        }

        log.info("Seeding Context Packs...");

        // Pack 1: Security Audit
        createPack("Security Audit Pack", 
            "Todo lo necesario para auditorías de seguridad en código", "security-lock", "security",
            List.of("Java Bug Fixer"), 
            List.of("Bug Hunter"), 
            List.of("GitHub", "Filesystem"),
            List.of("security", "audit", "owasp"));

        // Pack 2: AI Development
        createPack("AI Development Pack", 
            "Prompts y herramientas para construir mejores aplicaciones con IA", "ai-bot", "ai-development",
            List.of("Java Bug Fixer", "React Component Generator", "Unit Test Generator"), 
            List.of("Code Reviewer Expert", "API Documentation Writer", "Refactoring Assistant"), 
            List.of("Filesystem", "Fetch"),
            List.of("ai", "development", "prompts"));

        // Pack 3: Database Development
        createPack("Database Development Pack", 
            "Gestión, optimización y consultas de bases de datos", "database-storage", "database",
            List.of("React Component Generator"), 
            List.of("Test Generator Pro"), 
            List.of("PostgreSQL", "SQLite"),
            List.of("database", "sql", "backend"));

        // Pack 4: Content Writing
        createPack("Content Writing Pack", 
            "Herramientas para redactores y creadores de contenido", "writing-pen", "writing",
            List.of("Unit Test Generator"), 
            List.of("API Documentation Writer"), 
            List.of("Brave Search", "Google Drive"),
            List.of("writing", "content", "documentation"));

        log.info("Context Packs seeding finished. Total: {}", contextPackRepository.count());
    }

    private void createPack(String name, String desc, String emoji, String category,
                            List<String> promptNames, List<String> skillNames, List<String> mcpNames,
                            List<String> tags) {
        
        List<Long> promptIds = new ArrayList<>();
        for (String pName : promptNames) {
            promptRepository.findAll().stream()
                .filter(p -> p.getTitle().equalsIgnoreCase(pName))
                .findFirst()
                .ifPresentOrElse(
                    p -> {
                        promptIds.add(p.getId());
                        log.debug("Pack '{}': added prompt '{}' (ID:{})", name, pName, p.getId());
                    },
                    () -> log.warn("Pack '{}': prompt '{}' not found in DB", name, pName)
                );
        }

        List<Long> skillIds = new ArrayList<>();
        for (String sName : skillNames) {
            skillRepository.findAll().stream()
                .filter(s -> s.getName().equalsIgnoreCase(sName))
                .findFirst()
                .ifPresentOrElse(
                    s -> {
                        skillIds.add(s.getId());
                        log.debug("Pack '{}': added skill '{}' (ID:{})", name, sName, s.getId());
                    },
                    () -> log.warn("Pack '{}': skill '{}' not found in DB", name, sName)
                );
        }

        List<Long> mcpIds = new ArrayList<>();
        for (String mName : mcpNames) {
            mcpServerRepository.findAll().stream()
                .filter(m -> m.getName().equalsIgnoreCase(mName))
                .findFirst()
                .ifPresentOrElse(
                    m -> {
                        mcpIds.add(m.getId());
                        log.debug("Pack '{}': added mcp '{}' (ID:{})", name, mName, m.getId());
                    },
                    () -> log.warn("Pack '{}': mcp server '{}' not found in DB", name, mName)
                );
        }

        ContextPack pack = ContextPack.builder()
            .name(name)
            .description(desc)
            .emoji(emoji)
            .category(category)
            .promptIds(promptIds)
            .skillIds(skillIds)
            .mcpServerIds(mcpIds)
            .tags(tags)
            .setupInstructions("# Setup Instructions\n" +
                               "1. Copy the generated MCP configuration.\n" +
                               "2. Add it to your `claude_desktop_config.json`.\n" +
                               "3. Start using the prompts and skills included in this pack.")
            .usageCount(0)
            .build();

        contextPackRepository.save(pack);
    }
}
