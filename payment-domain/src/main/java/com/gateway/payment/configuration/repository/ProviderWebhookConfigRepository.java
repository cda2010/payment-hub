package com.gateway.payment.configuration.repository;

import com.gateway.payment.configuration.domain.ProviderWebhookConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderWebhookConfigRepository extends JpaRepository<ProviderWebhookConfig, UUID> {
    List<ProviderWebhookConfig> findByProviderId(UUID providerId);

    Optional<ProviderWebhookConfig> findByPathSuffix(String pathSuffix);
}
