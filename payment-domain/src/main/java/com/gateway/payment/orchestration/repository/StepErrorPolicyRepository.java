package com.gateway.payment.orchestration.repository;

import com.gateway.payment.orchestration.domain.StepErrorPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface StepErrorPolicyRepository extends JpaRepository<StepErrorPolicy, UUID> {

    @Query("""
            SELECT p FROM StepErrorPolicy p
            WHERE p.code = :code AND p.errorType = :errorType AND p.active = true
              AND p.schemaEcritureId IS NULL
            """)
    Optional<StepErrorPolicy> findActiveStepRule(String code, String errorType);

    @Query("""
            SELECT p FROM StepErrorPolicy p
            WHERE p.code = :code AND p.errorType = :errorType AND p.active = true
              AND p.schemaEcritureId = :schemaEcritureId
            """)
    Optional<StepErrorPolicy> findActiveSchemaRule(String code, String errorType, UUID schemaEcritureId);
}
