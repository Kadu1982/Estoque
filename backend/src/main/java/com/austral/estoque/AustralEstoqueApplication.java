package com.austral.estoque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableCaching
@EnableAsync
@EnableScheduling
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = "com.austral.estoque.domain")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "com.austral.estoque.repository")
public class AustralEstoqueApplication {
    public static void main(String[] args) {
        SpringApplication.run(AustralEstoqueApplication.class, args);
    }
}
