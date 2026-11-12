package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.Programme;
import com.gateway.payment.fidelite.domain.ProgrammeStatut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProgrammeRepository extends JpaRepository<Programme, UUID> {

    List<Programme> findByTenantIdAndStatutOrderByCreatedAtDesc(String tenantId, ProgrammeStatut statut);

    List<Programme> findByUniteIdAndStatutOrderByCreatedAtDesc(UUID uniteId, ProgrammeStatut statut);

    boolean existsByTenantIdAndCode(String tenantId, String code);
}
