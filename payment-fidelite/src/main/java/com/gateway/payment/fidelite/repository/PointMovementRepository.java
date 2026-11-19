package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.PointMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointMovementRepository extends JpaRepository<PointMovement, UUID> {

    boolean existsByTenantIdAndBusinessReference(String tenantId, String businessReference);

    Optional<PointMovement> findByTenantIdAndBusinessReference(String tenantId, String businessReference);

    List<PointMovement> findByTenantIdAndBusinessReferenceStartingWith(String tenantId, String prefix);

    List<PointMovement> findByAccount_IdOrderByCreatedAtDesc(UUID accountId);

    List<PointMovement> findByAccount_IdAndMovementTypeAndCreatedAtGreaterThanEqual(
            UUID accountId, String movementType, Instant since);
}
