package com.austral.estoque.dto.auth;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UUID userId;
    private String username;
    private String fullName;
    private String email;
    private List<String> roles;
}
