package com.promptvault.model;

/**
 * Categorías disponibles para servidores MCP.
 */
public enum MCPCategory {
    DEVELOPMENT("development", "Herramientas de desarrollo"),
    DATABASE("database", "Bases de datos"),
    PRODUCTIVITY("productivity", "Productividad y colaboración"),
    SEARCH("search", "Búsqueda web"),
    FILESYSTEM("filesystem", "Sistema de archivos"),
    AUTOMATION("automation", "Automatización"),
    WEB("web", "Acceso web y APIs"),
    UTILITY("utility", "Utilidades");
    
    private final String code;
    private final String displayName;
    
    MCPCategory(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
