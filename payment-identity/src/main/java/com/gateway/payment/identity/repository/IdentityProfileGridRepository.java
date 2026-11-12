package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityProfileGrid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IdentityProfileGridRepository extends JpaRepository<IdentityProfileGrid, UUID> {

    List<IdentityProfileGrid> findByProfileIdOrderByProduitCodeAsc(UUID profileId);

    List<IdentityProfileGrid> findByProfileIdIn(Collection<UUID> profileIds);

    void deleteByProfileId(UUID profileId);
}
