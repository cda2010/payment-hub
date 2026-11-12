package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityProfileLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IdentityProfileLimitRepository extends JpaRepository<IdentityProfileLimit, UUID> {

    List<IdentityProfileLimit> findByProfileIdOrderByProduitCodeAsc(UUID profileId);

    List<IdentityProfileLimit> findByProfileIdIn(Collection<UUID> profileIds);

    void deleteByProfileId(UUID profileId);
}
