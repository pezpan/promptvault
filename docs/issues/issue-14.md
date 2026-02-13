**Comando**: `Implementa ISSUE 14`

**Archivo**: `backend/src/main/java/com/promptvault/service/PromptService.java`

**Contenido** (COMPLETO, sin omisiones):

```java
package com.promptvault.service;

import com.promptvault.dto.PromptCreateRequest;
import com.promptvault.dto.PromptDTO;
import com.promptvault.dto.PromptUpdateRequest;
import com.promptvault.exception.ResourceNotFoundException;
import com.promptvault.model.Prompt;
import com.promptvault.repository.PromptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptService {
    
    private final PromptRepository promptRepository;
    
    @Transactional
    public PromptDTO createPrompt(PromptCreateRequest request) {
        log.info("Creando nuevo prompt: {}", request.getTitle());
        
        Prompt prompt = Prompt.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .content(request.getContent())
            .category(request.getCategory())
            .tags(request.getTags())
            .project(request.getProject())
            .isFavorite(false)
            .usageCount(0)
            .status("published")
            .build();
        
        Prompt saved = promptRepository.save(prompt);
        log.info("Prompt creado con ID: {}", saved.getId());
        
        return toDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public Page<PromptDTO> getAllPrompts(Pageable pageable) {
        return promptRepository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public PromptDTO getPromptById(Long id) {
        Prompt prompt = promptRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prompt", "id", id));
        return toDTO(prompt);
    }
    
    @Transactional
    public PromptDTO updatePrompt(Long id, PromptUpdateRequest request) {
        Prompt prompt = promptRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prompt", "id", id));
        
        if (request.getTitle() != null) {
            prompt.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            prompt.setDescription(request.getDescription());
        }
        if (request.getContent() != null) {
            prompt.setContent(request.getContent());
        }
        if (request.getCategory() != null) {
            prompt.setCategory(request.getCategory());
        }
        if (request.getTags() != null) {
            prompt.setTags(request.getTags());
        }
        if (request.getProject() != null) {
            prompt.setProject(request.getProject());
        }
        if (request.getIsFavorite() != null) {
            prompt.setIsFavorite(request.getIsFavorite());
        }
        
        Prompt updated = promptRepository.save(prompt);
        return toDTO(updated);
    }
    
    @Transactional
    public void deletePrompt(Long id) {
        if (!promptRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prompt", "id", id);
        }
        promptRepository.deleteById(id);
        log.info("Prompt eliminado: {}", id);
    }
    
    @Transactional(readOnly = true)
    public Page<PromptDTO> searchPrompts(String searchTerm, Pageable pageable) {
        return promptRepository.searchByTitleOrContent(searchTerm, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<PromptDTO> getPromptsByCategory(String category, Pageable pageable) {
        return promptRepository.findByCategory(category, pageable).map(this::toDTO);
    }
    
    private PromptDTO toDTO(Prompt prompt) {
        return PromptDTO.builder()
            .id(prompt.getId())
            .title(prompt.getTitle())
            .description(prompt.getDescription())
            .content(prompt.getContent())
            .category(prompt.getCategory())
            .tags(prompt.getTags())
            .project(prompt.getProject())
            .isFavorite(prompt.getIsFavorite())
            .usageCount(prompt.getUsageCount())
            .status(prompt.getStatus())
            .createdAt(prompt.getCreatedAt())
            .updatedAt(prompt.getUpdatedAt())
            .build();
    }
}
```

**Verificar**: Compilar sin errores