package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.LoyaltyRuleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LoyaltyRuleVersionRepository extends JpaRepository<LoyaltyRuleVersion, UUID> {

    @Query("""
            select v from LoyaltyRuleVersion v
            where v.rule.tenantId = :tenant
              and v.rule.programme.id = :programmeId
              and v.rule.statut = 'PUBLIE'
              and v.versionNo = (
                  select max(other.versionNo) from LoyaltyRuleVersion other where other.rule = v.rule
              )
            order by v.priority desc
            """)
    List<LoyaltyRuleVersion> published(@Param("tenant") String tenant, @Param("programmeId") UUID programmeId);

    @Query("select coalesce(max(v.versionNo), 0) from LoyaltyRuleVersion v where v.rule.id = :ruleId")
    int latest(@Param("ruleId") UUID ruleId);
}
