package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityAudience;
import com.gateway.payment.identity.domain.IdentityProfile;
import com.gateway.payment.identity.domain.ProfileKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityProfileRepository extends JpaRepository<IdentityProfile, UUID> {

    List<IdentityProfile> findByTenantIdAndActiveTrueOrderByCodeAsc(String tenantId);

    List<IdentityProfile> findByTenantIdAndAudienceAndActiveTrueOrderByCodeAsc(String tenantId, IdentityAudience audience);

    List<IdentityProfile> findByTenantIdAndKindAndActiveTrueOrderByCodeAsc(String tenantId, ProfileKind kind);

    List<IdentityProfile> findByTenantIdAndKindAndAudienceAndActiveTrueOrderByCodeAsc(
            String tenantId, ProfileKind kind, IdentityAudience audience);

    Optional<IdentityProfile> findByTenantIdAndCode(String tenantId, String code);

    List<IdentityProfile> findByIdIn(List<UUID> ids);
}
