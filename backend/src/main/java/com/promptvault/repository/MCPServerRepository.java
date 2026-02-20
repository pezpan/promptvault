package com.promptvault.repository;

import com.promptvault.model.MCPServer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad MCPServer.
 */
@Repository
public interface MCPServerRepository extends JpaRepository<MCPServer, Long> {
    
    /**
     * Busca servidores MCP por categoría.
     */
    Page<MCPServer> findByCategory(String category, Pageable pageable);
    
    /**
     * Busca servidores MCP que contengan un tag específico.
     */
    @Query(value = "SELECT * FROM mcp_servers m WHERE m.tags LIKE CONCAT('%,', :tag, ',%')", nativeQuery = true)
    Page<MCPServer> findServersByTag(@Param("tag") String tag, Pageable pageable);
    
    /**
     * Busca servidores MCP verificados.
     */
    Page<MCPServer> findByVerifiedTrue(Pageable pageable);
    
    /**
     * Búsqueda por texto en nombre y descripción.
     */
    @Query("SELECT m FROM MCPServer m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<MCPServer> searchByNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Obtiene los servidores más usados.
     */
    List<MCPServer> findTop10ByOrderByUsageCountDesc();
    
    /**
     * Busca servidores por categoría y verificados.
     */
    Page<MCPServer> findByCategoryAndVerifiedTrue(String category, Pageable pageable);

    // El MCP server más popular (por usageCount)
    Optional<MCPServer> findTopByOrderByUsageCountDesc();
}
