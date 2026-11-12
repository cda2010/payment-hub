package com.gateway.payment.configuration.repository;

import com.gateway.payment.configuration.domain.ProviderStatusMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderStatusMappingRepository extends JpaRepository<ProviderStatusMapping, UUID> {
    List<ProviderStatusMapping> findByProviderId(UUID providerId);

    Optional<ProviderStatusMapping> findByProviderIdAndProviderStatus(UUID providerId, String providerStatus);
}
