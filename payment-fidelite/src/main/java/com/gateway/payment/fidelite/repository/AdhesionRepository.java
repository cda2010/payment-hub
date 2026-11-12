package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.Adhesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdhesionRepository extends JpaRepository<Adhesion, UUID> {

    boolean existsByProgramme_IdAndPartyId(UUID programmeId, UUID partyId);

    Optional<Adhesion> findByProgramme_IdAndPartyId(UUID programmeId, UUID partyId);

    List<Adhesion> findByPartyId(UUID partyId);
}
