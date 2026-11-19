package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityApprovalPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityApprovalPolicyRepository extends JpaRepository<IdentityApprovalPolicy, UUID> {
    List<IdentityApprovalPolicy> findByTenantIdOrderByProductCodeAsc(String tenantId);

    Optional<IdentityApprovalPolicy> findByOrgNodeIdAndProductCode(UUID orgNodeId, String productCode);
}
