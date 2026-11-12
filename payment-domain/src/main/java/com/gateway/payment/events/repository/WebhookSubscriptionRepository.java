package com.gateway.payment.events.repository;

import com.gateway.payment.events.domain.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, UUID> {

    List<WebhookSubscription> findByTenantIdAndActiveTrue(String tenantId);

    List<WebhookSubscription> findByActiveTrue();

    Optional<WebhookSubscription> findByTenantIdAndCode(String tenantId, String code);
}
