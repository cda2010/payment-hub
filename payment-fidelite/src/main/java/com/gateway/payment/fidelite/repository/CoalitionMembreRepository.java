package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.CoalitionMembre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CoalitionMembreRepository extends JpaRepository<CoalitionMembre, UUID> {

    List<CoalitionMembre> findByProgramme_Id(UUID programmeId);

    boolean existsByProgramme_IdAndMarchandId(UUID programmeId, UUID marchandId);
}
