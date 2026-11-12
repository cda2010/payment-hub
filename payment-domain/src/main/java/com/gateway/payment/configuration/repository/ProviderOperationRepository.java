package com.gateway.payment.configuration.repository;

import com.gateway.payment.configuration.domain.ProviderOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderOperationRepository extends JpaRepository<ProviderOperation, UUID> {
    List<ProviderOperation> findByProviderId(UUID providerId);

    Optional<ProviderOperation> findByProviderIdAndCode(UUID providerId, String code);
}
