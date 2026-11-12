package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityAudience;
import com.gateway.payment.identity.domain.IdentityChannelUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityChannelUserRepository extends JpaRepository<IdentityChannelUser, UUID> {
    Optional<IdentityChannelUser> findByTenantIdAndUsername(String tenantId, String username);

    List<IdentityChannelUser> findByUsernameIgnoreCaseAndEnabledTrue(String username);

    List<IdentityChannelUser> findByTenantIdAndAudienceAndEnabledTrue(String tenantId, IdentityAudience audience);

    List<IdentityChannelUser> findByTenantIdAndEnabledTrueOrderByUsernameAsc(String tenantId);

    List<IdentityChannelUser> findByTenantIdAndAudienceOrderByUsernameAsc(String tenantId, IdentityAudience audience);

    List<IdentityChannelUser> findByTenantIdOrderByUsernameAsc(String tenantId);
}
