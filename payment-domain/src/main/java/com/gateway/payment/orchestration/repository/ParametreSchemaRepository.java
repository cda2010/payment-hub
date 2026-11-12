package com.gateway.payment.orchestration.repository;

import com.gateway.payment.orchestration.domain.ParametreSchema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ParametreSchemaRepository extends JpaRepository<ParametreSchema, UUID> {

    List<ParametreSchema> findByLigneSchemaEcritureIdOrderByCodeParametreAsc(UUID ligneId);
}
