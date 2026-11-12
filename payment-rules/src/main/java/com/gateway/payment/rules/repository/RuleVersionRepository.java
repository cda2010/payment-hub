package com.gateway.payment.rules.repository;

import com.gateway.payment.rules.domain.RuleStatus;
import com.gateway.payment.rules.domain.RuleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RuleVersionRepository extends JpaRepository<RuleVersion, UUID> {

    @Query("select v from RuleVersion v join fetch v.rule r where r.code = :code and v.status = :status order by v.version desc")
    List<RuleVersion> findByRuleCodeAndStatus(String code, RuleStatus status);

    default Optional<RuleVersion> findLatestPublished(String code) {
        return findByRuleCodeAndStatus(code, RuleStatus.PUBLIEE).stream().findFirst();
    }
}
