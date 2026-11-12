package com.gateway.payment.configuration.repository;

import com.gateway.payment.configuration.domain.ProviderOtpConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProviderOtpConfigRepository extends JpaRepository<ProviderOtpConfig, UUID> {
    Optional<ProviderOtpConfig> findByProviderId(UUID providerId);
}
