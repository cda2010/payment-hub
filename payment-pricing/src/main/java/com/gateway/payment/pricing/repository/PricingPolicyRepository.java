package com.gateway.payment.pricing.repository;

import com.gateway.payment.pricing.domain.PricingPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PricingPolicyRepository extends JpaRepository<PricingPolicy, UUID> {
    Optional<PricingPolicy> findByCodeAndActiveTrue(String code);

    List<PricingPolicy> findByActiveTrueOrderByPriorityDesc();

    List<PricingPolicy> findByProductCodeAndActiveTrueOrderByPriorityDesc(String productCode);
}
