package com.gateway.payment.configuration.repository;

import com.gateway.payment.configuration.domain.PaymentFlowStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentFlowStepRepository extends JpaRepository<PaymentFlowStep, UUID> {
    List<PaymentFlowStep> findByFlowIdOrderByOrdreAsc(UUID flowId);

    Optional<PaymentFlowStep> findByFlowIdAndCode(UUID flowId, String code);
}
