package com.gateway.payment.configuration.repository;

import com.gateway.payment.configuration.domain.ProviderEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderEnvironmentRepository extends JpaRepository<ProviderEnvironment, UUID> {
    Optional<ProviderEnvironment> findByProviderIdAndCode(UUID providerId, String code);

    List<ProviderEnvironment> findByProviderId(UUID providerId);
}
