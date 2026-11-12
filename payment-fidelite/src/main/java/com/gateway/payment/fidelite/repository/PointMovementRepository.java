package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.PointMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PointMovementRepository extends JpaRepository<PointMovement, UUID> {

    boolean existsByTenantIdAndBusinessReference(String tenantId, String businessReference);

    List<PointMovement> findByAccount_IdOrderByCreatedAtDesc(UUID accountId);
}
