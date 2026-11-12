package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.Recompense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecompenseRepository extends JpaRepository<Recompense, UUID> {

    List<Recompense> findByProgramme_IdOrderByPositionAsc(UUID programmeId);

    Optional<Recompense> findByProgramme_IdAndCodeIgnoreCase(UUID programmeId, String code);
}
