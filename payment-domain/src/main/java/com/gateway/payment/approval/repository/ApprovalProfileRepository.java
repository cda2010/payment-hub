package com.gateway.payment.approval.repository;

import com.gateway.payment.approval.domain.ApprovalProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApprovalProfileRepository extends JpaRepository<ApprovalProfileEntity, UUID> {
    Optional<ApprovalProfileEntity> findByCodeIgnoreCaseAndActifTrue(String code);

    Optional<ApprovalProfileEntity> findByCodeIgnoreCase(String code);
}
