package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.MissionProgres;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface MissionProgresRepository extends JpaRepository<MissionProgres, UUID> {

    Optional<MissionProgres> findByMission_IdAndPartyIdAndFenetreDebut(
            UUID missionId, UUID partyId, LocalDate fenetreDebut);
}
