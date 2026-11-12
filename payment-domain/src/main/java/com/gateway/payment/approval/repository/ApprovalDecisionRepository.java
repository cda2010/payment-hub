package com.gateway.payment.approval.repository;

import com.gateway.payment.approval.domain.ApprovalDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalDecisionRepository extends JpaRepository<ApprovalDecision, UUID> {
    List<ApprovalDecision> findByStepInstanceIdOrderByDecidedAtAsc(UUID stepInstanceId);
}
