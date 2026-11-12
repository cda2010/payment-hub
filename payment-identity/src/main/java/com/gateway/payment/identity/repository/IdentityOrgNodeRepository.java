package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityOrgNode;
import com.gateway.payment.identity.domain.OrgNodeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityOrgNodeRepository extends JpaRepository<IdentityOrgNode, UUID> {
    List<IdentityOrgNode> findByTenantIdAndActiveTrueOrderByNameAsc(String tenantId);

    List<IdentityOrgNode> findByTenantIdAndParentIdOrderByNameAsc(String tenantId, UUID parentId);

    List<IdentityOrgNode> findByTenantIdAndParentIdIsNullOrderByNameAsc(String tenantId);

    List<IdentityOrgNode> findByTenantIdAndTypeAndActiveTrue(String tenantId, OrgNodeType type);

    Optional<IdentityOrgNode> findByTenantIdAndCode(String tenantId, String code);
}
