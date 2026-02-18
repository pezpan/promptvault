package com.promptvault.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un servidor MCP (Model Context Protocol).
 * 
 * Los servidores MCP permiten conectar herramientas externas (GitHub, Slack, etc.)
 * a LLMs como Claude o GPT.
 */
@Entity
@Table(name = "mcp_servers", indexes = {
    @Index(name = "idx_verified", columnList = "verified"),
    @Index(name = "idx_usage_count", columnList = "usage_count")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MCPServer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 500)
    private String description;
    
    @Column(nullable = false, length = 50)
    private String category;
    
    @Column(name = "tags", length = 500)
    private String tags;
    
    @Column(nullable = false, length = 100)
    private String command;
    
    @Column(name = "args", columnDefinition = "TEXT")
    private String args;  // JSON array como string: ["arg1", "arg2"]
    
    @Column(name = "env_vars", columnDefinition = "TEXT")
    private String envVars;  // JSON object como string: {"KEY": "value"}
    
    @Column(name = "capabilities", columnDefinition = "TEXT")
    private String capabilities;  // JSON array: ["read", "write"]
    
    @Column(name = "documentation", length = 500)
    private String documentation;
    
    @Column(name = "official_url", length = 500)
    private String officialUrl;
    
    @Column(name = "installation_instructions", columnDefinition = "TEXT")
    private String installationInstructions;
    
    @Column(name = "config_example", columnDefinition = "TEXT")
    private String configExample;
    
    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;
    
    @Column(name = "rating")
    private Double rating;
    
    @Column(name = "verified")
    @Builder.Default
    private Boolean verified = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
