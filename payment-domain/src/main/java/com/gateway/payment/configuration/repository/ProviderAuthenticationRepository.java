package com.gateway.payment.configuration.repository;

import com.gateway.payment.configuration.domain.ProviderAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProviderAuthenticationRepository extends JpaRepository<ProviderAuthentication, UUID> {
    Optional<ProviderAuthentication> findByProviderIdAndEnvironmentId(UUID providerId, UUID environmentId);
}
