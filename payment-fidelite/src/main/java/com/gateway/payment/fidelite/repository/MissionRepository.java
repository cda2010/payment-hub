package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissionRepository extends JpaRepository<Mission, UUID> {

    List<Mission> findByProgramme_IdAndActifTrueOrderByCodeAsc(UUID programmeId);

    Optional<Mission> findByIdAndProgramme_Id(UUID id, UUID programmeId);

    List<Mission> findByTenantIdAndActifTrue(String tenantId);
}
