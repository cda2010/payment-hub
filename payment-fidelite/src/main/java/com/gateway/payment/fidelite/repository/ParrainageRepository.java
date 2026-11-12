package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.Parrainage;
import com.gateway.payment.fidelite.domain.ParrainageStatut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParrainageRepository extends JpaRepository<Parrainage, UUID> {

    Optional<Parrainage> findByCodeIgnoreCase(String code);

    boolean existsByCode(String code);

    boolean existsByProgramme_IdAndFilleulPartyId(UUID programmeId, UUID filleulPartyId);

    List<Parrainage> findByFilleulPartyIdAndStatut(UUID filleulPartyId, ParrainageStatut statut);

    long countByProgramme_IdAndParrainPartyIdAndStatutAndQualifiedAtGreaterThanEqual(
            UUID programmeId,
            UUID parrainPartyId,
            ParrainageStatut statut,
            Instant qualifiedAt
    );
}
