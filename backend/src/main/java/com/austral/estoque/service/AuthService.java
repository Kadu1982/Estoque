package com.austral.estoque.service;

import com.austral.estoque.domain.user.Role;
import com.austral.estoque.domain.user.User;
import com.austral.estoque.dto.auth.*;
import com.austral.estoque.exception.BusinessException;
import com.austral.estoque.repository.user.RoleRepository;
import com.austral.estoque.repository.user.UserRepository;
import com.austral.estoque.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements ApplicationRunner {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email}")
    private String adminEmail;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLogin());
        String accessToken = jwtTokenProvider.generateToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        User user = userRepository.findByUsernameOrEmail(request.getLogin(), request.getLogin())
            .orElseThrow();

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .userId(user.getId())
            .username(user.getUsername())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .roles(user.getRoles().stream().map(Role::getName).toList())
            .build();
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        try {
            String username = jwtTokenProvider.extractUsername(request.getRefreshToken());
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!jwtTokenProvider.isTokenValid(request.getRefreshToken(), userDetails)) {
                throw new BusinessException("Token de refresh inválido ou expirado");
            }
            String newAccessToken = jwtTokenProvider.generateToken(userDetails);
            User user = userRepository.findByUsernameOrEmail(username, username).orElseThrow();
            return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
        } catch (Exception e) {
            throw new BusinessException("Token de refresh inválido");
        }
    }

    // Seed do usuário admin ao iniciar
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Criando usuário admin inicial: {}", adminEmail);
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder().name("ADMIN").description("Administrador do sistema").build()
                    ));

            User admin = User.builder()
                .username("admin")
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .fullName("Administrador")
                .authProvider(User.AuthProvider.LOCAL)
                .active(true)
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();

            userRepository.save(admin);
            log.info("Admin criado com sucesso.");
        }
    }
}
