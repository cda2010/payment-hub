package com.gateway.payment.rules.repository;

import com.gateway.payment.rules.domain.LimitCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface LimitCounterRepository extends JpaRepository<LimitCounter, UUID> {

    Optional<LimitCounter> findByLimitCodeAndScopeKeyAndPeriodKey(String limitCode, String scopeKey, String periodKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c from LimitCounter c
            where c.limitCode = :limitCode and c.scopeKey = :scopeKey and c.periodKey = :periodKey
            """)
    Optional<LimitCounter> findForUpdate(String limitCode, String scopeKey, String periodKey);
}
