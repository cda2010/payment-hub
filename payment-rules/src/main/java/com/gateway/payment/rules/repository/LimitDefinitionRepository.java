package com.gateway.payment.rules.repository;

import com.gateway.payment.rules.domain.LimitDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LimitDefinitionRepository extends JpaRepository<LimitDefinition, UUID> {
    Optional<LimitDefinition> findByCode(String code);
    Optional<LimitDefinition> findByCodeAndActiveTrue(String code);
    List<LimitDefinition> findByActiveTrueOrderByCodeAsc();
}
