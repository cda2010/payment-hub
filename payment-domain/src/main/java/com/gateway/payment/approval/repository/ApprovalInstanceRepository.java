package com.gateway.payment.approval.repository;

import com.gateway.payment.approval.domain.ApprovalInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApprovalInstanceRepository extends JpaRepository<ApprovalInstance, UUID> {
    Optional<ApprovalInstance> findByPaymentId(String paymentId);
}
