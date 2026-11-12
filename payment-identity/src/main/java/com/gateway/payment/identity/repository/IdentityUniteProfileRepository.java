package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityUniteProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityUniteProfileRepository extends JpaRepository<IdentityUniteProfile, UUID> {

    List<IdentityUniteProfile> findByUniteIdAndActiveTrueOrderByDateDebutDesc(UUID uniteId);

    List<IdentityUniteProfile> findByUniteIdOrderByDateDebutDesc(UUID uniteId);

    Optional<IdentityUniteProfile> findByUniteIdAndProfileId(UUID uniteId, UUID profileId);
}
