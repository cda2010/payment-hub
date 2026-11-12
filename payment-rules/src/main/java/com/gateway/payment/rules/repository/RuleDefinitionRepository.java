package com.gateway.payment.rules.repository;

import com.gateway.payment.rules.domain.RuleDefinition;
import com.gateway.payment.rules.domain.RuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RuleDefinitionRepository extends JpaRepository<RuleDefinition, UUID> {
    Optional<RuleDefinition> findByCode(String code);
    List<RuleDefinition> findByStatusOrderByCodeAsc(RuleStatus status);
    List<RuleDefinition> findAllByOrderByCodeAsc();

    @Query("select distinct r from RuleDefinition r left join fetch r.versions where r.id = :id")
    Optional<RuleDefinition> findDetailedById(UUID id);
}
