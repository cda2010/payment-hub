package com.gateway.payment.commission.repository;

import com.gateway.payment.commission.domain.GrilleCommission;
import com.gateway.payment.commission.domain.StatutGrilleCommission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GrilleCommissionRepository extends JpaRepository<GrilleCommission, UUID> {

    List<GrilleCommission> findByStatusOrderByCodeAscVersionDesc(StatutGrilleCommission status);

    List<GrilleCommission> findAllByOrderByCodeAscVersionDesc();

    Optional<GrilleCommission> findByCodeAndVersion(String code, int version);

    List<GrilleCommission> findByOperationTypeAndStatusOrderByVersionDesc(
            String operationType, StatutGrilleCommission status);
}
