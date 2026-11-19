package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityApprovalAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface IdentityApprovalAssigneeRepository extends JpaRepository<IdentityApprovalAssignee, UUID> {
    List<IdentityApprovalAssignee> findByPolicyIdOrderByLevelAsc(UUID policyId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void deleteByPolicyId(UUID policyId);
}
