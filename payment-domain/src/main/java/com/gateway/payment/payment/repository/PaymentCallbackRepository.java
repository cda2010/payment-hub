package com.gateway.payment.payment.repository;

import com.gateway.payment.payment.domain.PaymentCallback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentCallbackRepository extends JpaRepository<PaymentCallback, UUID> {

    Optional<PaymentCallback> findByProviderCodeAndExternalEventId(String providerCode, String externalEventId);

    List<PaymentCallback> findByPaymentIdOrderByCreatedAtDesc(String paymentId);
}
