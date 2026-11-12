package com.gateway.payment.commission.repository;

import com.gateway.payment.commission.domain.LiquidationCommission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LiquidationCommissionRepository extends JpaRepository<LiquidationCommission, UUID> {
}
