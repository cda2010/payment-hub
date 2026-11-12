package com.gateway.payment.commission.repository;

import com.gateway.payment.commission.domain.FormuleCommission;
import com.gateway.payment.commission.domain.StatutFormuleCommission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormuleCommissionRepository extends JpaRepository<FormuleCommission, UUID> {
    List<FormuleCommission> findByStatusOrderByCodeAscVersionAsc(StatutFormuleCommission status);

    Optional<FormuleCommission> findByCodeAndVersion(String code, int version);
}
