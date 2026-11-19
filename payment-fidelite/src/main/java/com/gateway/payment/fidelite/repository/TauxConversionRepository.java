package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.TauxConversion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TauxConversionRepository extends JpaRepository<TauxConversion, UUID> {

    Optional<TauxConversion> findFirstByTenantIdAndSource_IdAndCible_IdAndActifTrue(
            String tenantId, UUID sourceId, UUID cibleId);

    Optional<TauxConversion> findFirstByTenantIdAndSource_IdAndCibleIsNullAndActifTrue(
            String tenantId, UUID sourceId);
}
