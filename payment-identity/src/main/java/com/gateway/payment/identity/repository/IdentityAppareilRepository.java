package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityAppareil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityAppareilRepository extends JpaRepository<IdentityAppareil, UUID> {

    Optional<IdentityAppareil> findByTenantIdAndAppareilId(String tenantId, String appareilId);

    List<IdentityAppareil> findByPartyIdOrderByLastSeenAtDesc(UUID partyId);
}
