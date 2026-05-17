package com.austral.estoque.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionGuard {

    private static final String DEFAULT_JWT_SECRET = "dev_secret_key_change_in_production_min_64_chars_here_ok";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123456";

    public ProductionGuard(
        @Value("${security.jwt.secret}") String jwtSecret,
        @Value("${app.seed.admin-password}") String adminPassword
    ) {
        if (jwtSecret == null || jwtSecret.length() < 64 || DEFAULT_JWT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException("Invalid production configuration: JWT secret must be provided and have at least 64 characters.");
        }

        if (adminPassword == null || adminPassword.isBlank() || DEFAULT_ADMIN_PASSWORD.equals(adminPassword)) {
            throw new IllegalStateException("Invalid production configuration: seeded admin password must be changed from default.");
        }
    }
}
