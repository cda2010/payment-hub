package com.gateway.payment.security.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.security.api.dto.AuthResponse;
import com.gateway.payment.security.api.dto.LoginRequest;
import com.gateway.payment.security.config.SecurityProperties;
import com.gateway.payment.security.domain.GatewayUser;
import com.gateway.payment.security.jwt.JwtService;
import com.gateway.payment.security.repository.GatewayUserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final GatewayUserRepository userRepository;
    private final JwtService jwtService;
    private final SecurityProperties securityProperties;
    private final AuditService auditService;

    public AuthService(
            AuthenticationManager authenticationManager,
            GatewayUserRepository userRepository,
            JwtService jwtService,
            SecurityProperties securityProperties,
            AuditService auditService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.securityProperties = securityProperties;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        GatewayUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur introuvable", HttpStatus.UNAUTHORIZED));

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        auditService.record(user.getUsername(), "LOGIN", "USER", user.getId().toString(), "Connexion réussie");

        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parse(refreshToken);
        } catch (Exception ex) {
            throw new BusinessException("INVALID_TOKEN", "Refresh token invalide", HttpStatus.UNAUTHORIZED);
        }

        if (!jwtService.isRefreshToken(claims)) {
            throw new BusinessException("INVALID_TOKEN", "Refresh token invalide", HttpStatus.UNAUTHORIZED);
        }

        GatewayUser user = userRepository.findByUsername(claims.getSubject())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur introuvable", HttpStatus.UNAUTHORIZED));

        if (!user.isEnabled() || user.isLocked()) {
            throw new BusinessException("ACCOUNT_DISABLED", "Compte désactivé", HttpStatus.UNAUTHORIZED);
        }

        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(GatewayUser user) {
        return new AuthResponse(
                jwtService.createAccessToken(user),
                jwtService.createRefreshToken(user),
                "Bearer",
                securityProperties.jwt().accessTokenTtl().toSeconds(),
                new AuthResponse.UserInfo(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRoles().stream().map(r -> r.getCode()).toList()
                )
        );
    }
}
