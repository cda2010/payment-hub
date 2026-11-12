package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.IdentityMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityMembershipRepository extends JpaRepository<IdentityMembership, UUID> {
    List<IdentityMembership> findByUserIdAndActiveTrue(UUID userId);

    List<IdentityMembership> findByUserId(UUID userId);

    List<IdentityMembership> findByOrgNodeIdAndActiveTrue(UUID orgNodeId);

    boolean existsByUserIdAndOrgNodeId(UUID userId, UUID orgNodeId);

    Optional<IdentityMembership> findByUserIdAndOrgNodeId(UUID userId, UUID orgNodeId);
}
