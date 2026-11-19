package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.PlafondPoints;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlafondPointsRepository extends JpaRepository<PlafondPoints, UUID> {

    Optional<PlafondPoints> findByTenantIdAndProgrammeIdAndPartyIdAndPorteeAndPeriode(
            String tenantId, UUID programmeId, UUID partyId, String portee, String periode);
}
