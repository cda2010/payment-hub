package com.gateway.payment.approval.repository;

import com.gateway.payment.approval.domain.ApprovalStepInstance;
import com.gateway.payment.approval.domain.ApprovalStepStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApprovalStepInstanceRepository extends JpaRepository<ApprovalStepInstance, UUID> {
    List<ApprovalStepInstance> findByApprovalInstanceIdOrderByLevelNumberAsc(UUID approvalInstanceId);

    Optional<ApprovalStepInstance> findByApprovalInstanceIdAndLevelNumber(UUID approvalInstanceId, int levelNumber);

    Optional<ApprovalStepInstance> findFirstByApprovalInstanceIdAndStatusOrderByLevelNumberAsc(
            UUID approvalInstanceId, ApprovalStepStatus status);

    List<ApprovalStepInstance> findByStatusOrderByCreatedAtAsc(ApprovalStepStatus status);
}
