package com.gateway.payment.security.bootstrap;

import com.gateway.payment.security.domain.GatewayRole;
import com.gateway.payment.security.domain.GatewayUser;
import com.gateway.payment.security.domain.RoleCode;
import com.gateway.payment.security.repository.GatewayRoleRepository;
import com.gateway.payment.security.repository.GatewayUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final GatewayUserRepository userRepository;
    private final GatewayRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(
            GatewayUserRepository userRepository,
            GatewayRoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername("admin")) {
            return;
        }

        GatewayRole adminRole = roleRepository.findByCode(RoleCode.ADMIN.name())
                .orElseThrow(() -> new IllegalStateException("Rôle ADMIN manquant — vérifier Flyway V1"));

        GatewayUser admin = GatewayUser.builder()
                .username("admin")
                .email("admin@gateway.local")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .fullName("Administrateur Gateway")
                .enabled(true)
                .locked(false)
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);
        log.info("Compte admin initial créé (username=admin). Changez le mot de passe en production.");
    }
}
