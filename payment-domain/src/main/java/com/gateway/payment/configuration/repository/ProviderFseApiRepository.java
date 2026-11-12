package com.gateway.payment.configuration.repository;

import com.gateway.payment.configuration.domain.ProviderFseApi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderFseApiRepository extends JpaRepository<ProviderFseApi, UUID> {
    List<ProviderFseApi> findByProviderId(UUID providerId);

    Optional<ProviderFseApi> findByProviderIdAndCode(UUID providerId, String code);
}
