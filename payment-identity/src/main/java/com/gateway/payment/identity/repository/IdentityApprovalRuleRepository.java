package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityApprovalRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface IdentityApprovalRuleRepository extends JpaRepository<IdentityApprovalRule, UUID> {
    List<IdentityApprovalRule> findByPolicyIdOrderByPositionAsc(UUID policyId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void deleteByPolicyId(UUID policyId);
}
