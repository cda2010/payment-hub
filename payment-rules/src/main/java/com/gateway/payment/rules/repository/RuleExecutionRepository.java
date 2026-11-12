package com.gateway.payment.rules.repository;

import com.gateway.payment.rules.domain.RuleExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RuleExecutionRepository extends JpaRepository<RuleExecution, UUID> {
}
