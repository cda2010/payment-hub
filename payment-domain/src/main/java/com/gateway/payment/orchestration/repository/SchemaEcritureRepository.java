package com.gateway.payment.orchestration.repository;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.orchestration.domain.SchemaEcriture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchemaEcritureRepository extends JpaRepository<SchemaEcriture, UUID> {

    List<SchemaEcriture> findByServiceTypeAndActifTrueOrderByNomSchemaAsc(ServiceTypeCode serviceType);

    List<SchemaEcriture> findAllByOrderByServiceTypeAscNomSchemaAsc();

    Optional<SchemaEcriture> findByServiceTypeAndPatternCode(ServiceTypeCode serviceType, String patternCode);

    @Query("""
            SELECT DISTINCT s FROM SchemaEcriture s
            LEFT JOIN FETCH s.lignes l
            WHERE s.id = :id
            ORDER BY l.ordre ASC
            """)
    Optional<SchemaEcriture> findByIdWithLignes(UUID id);
}
