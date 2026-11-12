package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityProfileService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IdentityProfileServiceRepository extends JpaRepository<IdentityProfileService, UUID> {

    List<IdentityProfileService> findByProfileIdOrderByServiceCodeAsc(UUID profileId);

    List<IdentityProfileService> findByProfileIdInAndActiveTrue(Collection<UUID> profileIds);

    void deleteByProfileId(UUID profileId);
}
