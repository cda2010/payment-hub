package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.InboundEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InboundEventRepository extends JpaRepository<InboundEvent, UUID> {

    Optional<InboundEvent> findByTenantIdAndEventId(String tenantId, UUID eventId);

    boolean existsByTenantIdAndCustomerIdAndEventTypeIgnoreCase(String tenantId, String customerId, String eventType);
}
