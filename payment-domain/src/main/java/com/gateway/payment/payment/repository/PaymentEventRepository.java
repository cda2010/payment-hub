package com.gateway.payment.payment.repository;

import com.gateway.payment.payment.domain.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, UUID> {

    List<PaymentEvent> findByPaymentIdOrderByCreatedAtAsc(String paymentId);
}
