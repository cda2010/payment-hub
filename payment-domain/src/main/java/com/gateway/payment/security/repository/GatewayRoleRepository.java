package com.gateway.payment.security.repository;

import com.gateway.payment.security.domain.GatewayRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GatewayRoleRepository extends JpaRepository<GatewayRole, UUID> {
    Optional<GatewayRole> findByCode(String code);
}
