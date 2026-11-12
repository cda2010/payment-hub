package com.gateway.payment.security.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.security.api.dto.CreateUserRequest;
import com.gateway.payment.security.api.dto.UserResponse;
import com.gateway.payment.security.domain.GatewayRole;
import com.gateway.payment.security.domain.GatewayUser;
import com.gateway.payment.security.repository.GatewayRoleRepository;
import com.gateway.payment.security.repository.GatewayUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserManagementService {

    private final GatewayUserRepository userRepository;
    private final GatewayRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserManagementService(
            GatewayUserRepository userRepository,
            GatewayRoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public UserResponse create(CreateUserRequest request, String actor) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("USERNAME_EXISTS", "Nom d'utilisateur déjà utilisé");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("EMAIL_EXISTS", "Email déjà utilisé");
        }

        Set<GatewayRole> roles = resolveRoles(request.roles());

        GatewayUser user = GatewayUser.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .enabled(true)
                .locked(false)
                .roles(roles)
                .build();

        GatewayUser saved = userRepository.save(user);
        auditService.record(actor, "USER_CREATE", "USER", saved.getId().toString(),
                "Création utilisateur " + saved.getUsername());
        return toResponse(saved);
    }

    @Transactional
    public UserResponse setEnabled(UUID id, boolean enabled, String actor) {
        GatewayUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur introuvable", HttpStatus.NOT_FOUND));
        user.setEnabled(enabled);
        GatewayUser saved = userRepository.save(user);
        auditService.record(actor, enabled ? "USER_ENABLE" : "USER_DISABLE", "USER", id.toString(), null);
        return toResponse(saved);
    }

    private Set<GatewayRole> resolveRoles(Set<String> roleCodes) {
        Set<GatewayRole> roles = new HashSet<>();
        for (String code : roleCodes) {
            GatewayRole role = roleRepository.findByCode(code)
                    .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND", "Rôle inconnu: " + code));
            roles.add(role);
        }
        return roles;
    }

    private UserResponse toResponse(GatewayUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.isEnabled(),
                user.isLocked(),
                user.getRoles().stream().map(GatewayRole::getCode).sorted().collect(Collectors.toList()),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
