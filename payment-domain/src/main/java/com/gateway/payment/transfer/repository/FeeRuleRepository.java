package com.gateway.payment.transfer.repository;

import com.gateway.payment.transfer.domain.FeeRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeeRuleRepository extends JpaRepository<FeeRule, UUID> {
    Optional<FeeRule> findByCodeIgnoreCase(String code);
    List<FeeRule> findAllByOrderByCodeAsc();
    boolean existsByCodeIgnoreCase(String code);
}

