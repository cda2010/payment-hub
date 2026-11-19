package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.NotificationFidelite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationFideliteRepository extends JpaRepository<NotificationFidelite, UUID> {

    boolean existsByTenantIdAndDedupKeyAndCanal(String tenantId, String dedupKey, String canal);

    List<NotificationFidelite> findByTenantIdAndPartyIdOrderByCreatedAtDesc(String tenantId, UUID partyId);

    List<NotificationFidelite> findByTenantIdOrderByCreatedAtDesc(String tenantId);
}
