package com.austral.estoque.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String login; // username ou email
    private String password;
}
