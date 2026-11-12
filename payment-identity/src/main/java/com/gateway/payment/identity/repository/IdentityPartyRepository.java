package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityPartyRepository extends JpaRepository<IdentityParty, UUID>,
        JpaSpecificationExecutor<IdentityParty> {
    List<IdentityParty> findByTenantIdAndActiveTrueOrderByDisplayNameAsc(String tenantId);

    Optional<IdentityParty> findByTenantIdAndCode(String tenantId, String code);

    List<IdentityParty> findByTenantIdAndDisplayNameContainingIgnoreCase(String tenantId, String q);
}
