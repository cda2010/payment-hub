package com.gateway.payment.security.repository;

import com.gateway.payment.security.domain.GatewayUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GatewayUserRepository extends JpaRepository<GatewayUser, UUID> {
    Optional<GatewayUser> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
