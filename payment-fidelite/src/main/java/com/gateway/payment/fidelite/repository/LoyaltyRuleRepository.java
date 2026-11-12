package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.LoyaltyRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoyaltyRuleRepository extends JpaRepository<LoyaltyRule, UUID> {

    Optional<LoyaltyRule> findByTenantIdAndCode(String tenantId, String code);
}
