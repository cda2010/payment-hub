package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityPartyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityPartyProfileRepository extends JpaRepository<IdentityPartyProfile, UUID> {

    List<IdentityPartyProfile> findByPartyIdAndActiveTrueOrderByDateDebutDesc(UUID partyId);

    List<IdentityPartyProfile> findByPartyIdOrderByDateDebutDesc(UUID partyId);

    Optional<IdentityPartyProfile> findByPartyIdAndProfileId(UUID partyId, UUID profileId);

    boolean existsByPartyIdAndProfileId(UUID partyId, UUID profileId);
}
