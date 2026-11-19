package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.SegmentClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SegmentClientRepository extends JpaRepository<SegmentClient, UUID> {

    List<SegmentClient> findByTenantIdAndPartyId(String tenantId, UUID partyId);

    void deleteByTenantIdAndPartyId(String tenantId, UUID partyId);
}
