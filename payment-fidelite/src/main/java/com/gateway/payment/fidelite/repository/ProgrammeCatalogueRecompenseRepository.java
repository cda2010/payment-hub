package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.ProgrammeCatalogueRecompense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProgrammeCatalogueRecompenseRepository extends JpaRepository<ProgrammeCatalogueRecompense, UUID> {

    @EntityGraph(attributePaths = "catalogue")
    List<ProgrammeCatalogueRecompense> findByProgramme_IdOrderByPositionAsc(UUID programmeId);

    @EntityGraph(attributePaths = "catalogue")
    Optional<ProgrammeCatalogueRecompense> findByProgramme_IdAndCatalogue_CodeIgnoreCase(UUID programmeId, String code);
}
