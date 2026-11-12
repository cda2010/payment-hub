package com.gateway.payment.rules.repository;

import com.gateway.payment.rules.domain.RuleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RuleGroupRepository extends JpaRepository<RuleGroup, UUID> {

    Optional<RuleGroup> findByCode(String code);

    List<RuleGroup> findAllByOrderByCodeAsc();

    List<RuleGroup> findByStatusOrderByCodeAsc(String status);

    @Query("""
            select distinct g from RuleGroup g
            left join fetch g.items i
            left join fetch i.ruleVersion v
            left join fetch v.rule
            where g.code = :code
            """)
    Optional<RuleGroup> findDetailedByCode(String code);

    @Query("""
            select distinct g from RuleGroup g
            left join fetch g.items i
            left join fetch i.ruleVersion v
            left join fetch v.rule
            where g.status = 'ACTIVE'
              and (g.transactionType is null or g.transactionType = :txType)
            order by g.code
            """)
    List<RuleGroup> findActiveForTransactionType(String txType);
}
