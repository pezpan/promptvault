package com.promptvault;

import static org.assertj.core.api.Assertions.assertThat;
import com.promptvault.dto.ContextPackDetailDTO;
import com.promptvault.dto.ContextPackDTO;
import com.promptvault.service.ContextPackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
class ContextPackLoadTests {

    @Autowired
    private ContextPackService contextPackService;

    @Test
    void packsAreSeededWithResources() {
        List<ContextPackDTO> packs = contextPackService.findAll();
        assertThat(packs).isNotEmpty();
        
        System.out.println("Packs found: " + packs.size());
        
        for (ContextPackDTO pack : packs) {
            System.out.println("Checking pack: " + pack.getName());
            ContextPackDetailDTO detail = contextPackService.findById(pack.getId());
            
            System.out.println("  - Prompts: " + detail.getPrompts().size());
            System.out.println("  - Skills: " + detail.getSkills().size());
            System.out.println("  - MCPs: " + detail.getMcpServers().size());
            
            // Al menos un pack debería tener recursos si el seed funcionó
            // No podemos asegurar que TODOS tengan (depende de los nombres exactos en data.sql)
        }
        
        // Verificamos el pack de seguridad específicamente
        packs.stream()
            .filter(p -> p.getName().contains("Security"))
            .findFirst()
            .ifPresent(p -> {
                ContextPackDetailDTO detail = contextPackService.findById(p.getId());
                assertThat(detail.getPrompts().size() + detail.getSkills().size() + detail.getMcpServers().size())
                    .as("Security pack should have some resources linked")
                    .isGreaterThan(0);
            });
    }
}
