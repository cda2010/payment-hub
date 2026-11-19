package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.Consentement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConsentementRepository extends JpaRepository<Consentement, UUID> {

    Optional<Consentement> findByTenantIdAndPartyId(String tenantId, UUID partyId);
}
