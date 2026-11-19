package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContributionRepository extends JpaRepository<Contribution, UUID> {

    List<Contribution> findByProgrammeId(UUID programmeId);

    boolean existsByTenantIdAndBusinessReference(String tenantId, String businessReference);
}
