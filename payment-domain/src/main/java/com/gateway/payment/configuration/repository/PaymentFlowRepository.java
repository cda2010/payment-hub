package com.gateway.payment.configuration.repository;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.domain.PaymentFlow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentFlowRepository extends JpaRepository<PaymentFlow, UUID> {
    List<PaymentFlow> findByProviderId(UUID providerId);

    List<PaymentFlow> findByProviderIdAndActifTrue(UUID providerId);

    Optional<PaymentFlow> findByProviderIdAndCode(UUID providerId, String code);

    Optional<PaymentFlow> findByProviderIdAndServiceType(UUID providerId, ServiceTypeCode serviceType);

    Optional<PaymentFlow> findFirstByProviderIdAndBpmProcessKeyAndActifTrue(UUID providerId, String bpmProcessKey);

    Optional<PaymentFlow> findFirstByBpmProcessKeyAndActifTrue(String bpmProcessKey);
}
