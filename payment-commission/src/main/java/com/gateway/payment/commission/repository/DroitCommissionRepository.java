package com.gateway.payment.commission.repository;

import com.gateway.payment.commission.domain.DroitCommission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DroitCommissionRepository extends JpaRepository<DroitCommission, UUID> {

    boolean existsByTransactionId(String transactionId);

    List<DroitCommission> findByTransactionId(String transactionId);

    List<DroitCommission> findByStatusAndBeneficiaryRoleAndCurrency(
            String status, String beneficiaryRole, String currency);
}
