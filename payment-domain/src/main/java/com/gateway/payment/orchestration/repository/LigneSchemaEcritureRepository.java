package com.gateway.payment.orchestration.repository;

import com.gateway.payment.orchestration.domain.LigneSchemaEcriture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LigneSchemaEcritureRepository extends JpaRepository<LigneSchemaEcriture, UUID> {

    List<LigneSchemaEcriture> findBySchemaEcritureIdOrderByOrdreAsc(UUID schemaEcritureId);

    @Query("""
            SELECT DISTINCT l FROM LigneSchemaEcriture l
            LEFT JOIN FETCH l.parametres
            WHERE l.schemaEcriture.id = :schemaId
            ORDER BY l.ordre ASC
            """)
    List<LigneSchemaEcriture> findBySchemaIdWithParametres(UUID schemaId);

    Optional<LigneSchemaEcriture> findBySchemaEcritureIdAndStepId(UUID schemaId, String stepId);

    @Query("""
            SELECT DISTINCT l FROM LigneSchemaEcriture l
            LEFT JOIN FETCH l.parametres
            LEFT JOIN FETCH l.schemaEcriture
            WHERE l.id = :id
            """)
    Optional<LigneSchemaEcriture> findByIdWithParametres(UUID id);
}
