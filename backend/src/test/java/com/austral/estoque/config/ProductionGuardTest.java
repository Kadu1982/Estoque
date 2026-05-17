package com.austral.estoque.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionGuardTest {

    private static final String VALID_JWT_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void shouldFailWhenJwtSecretIsTooShort() {
        assertThrows(IllegalStateException.class, () -> new ProductionGuard("short-secret", "StrongPass@123"));
    }

    @Test
    void shouldFailWhenJwtSecretIsDefaultValue() {
        String defaultSecret = "dev_secret_key_change_in_production_min_64_chars_here_ok";
        assertThrows(IllegalStateException.class, () -> new ProductionGuard(defaultSecret, "StrongPass@123"));
    }

    @Test
    void shouldFailWhenAdminPasswordIsDefaultValue() {
        assertThrows(IllegalStateException.class, () -> new ProductionGuard(VALID_JWT_SECRET, "Admin@123456"));
    }

    @Test
    void shouldFailWhenAdminPasswordIsBlank() {
        assertThrows(IllegalStateException.class, () -> new ProductionGuard(VALID_JWT_SECRET, " "));
    }

    @Test
    void shouldAllowValidProductionValues() {
        assertDoesNotThrow(() -> new ProductionGuard(VALID_JWT_SECRET, "StrongPass@123"));
    }
}
