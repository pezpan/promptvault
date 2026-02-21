package com.promptvault.config;

import com.promptvault.service.ContextPackSeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SeedConfig implements ApplicationRunner {

    private final ContextPackSeedService contextPackSeedService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        contextPackSeedService.seed();
    }
}
