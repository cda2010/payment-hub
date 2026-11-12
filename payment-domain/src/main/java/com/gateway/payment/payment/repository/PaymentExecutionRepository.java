package com.gateway.payment.payment.repository;

import com.gateway.payment.payment.domain.PaymentExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PaymentExecutionRepository extends JpaRepository<PaymentExecution, UUID> {

    List<PaymentExecution> findByPaymentIdOrderByCreatedAtAsc(String paymentId);

    @Query("""
            SELECT e.providerCode AS providerCode,
                   e.operationCode AS operationCode,
                   SUM(CASE WHEN e.success = true THEN 1 ELSE 0 END) AS successCount,
                   SUM(CASE WHEN e.success = false THEN 1 ELSE 0 END) AS failureCount,
                   AVG(e.executionDurationMs) AS avgDurationMs
            FROM PaymentExecution e
            WHERE e.requestTimestamp >= :since
            GROUP BY e.providerCode, e.operationCode
            ORDER BY e.providerCode, e.operationCode
            """)
    List<ProviderExecutionSummary> summarizeSince(@Param("since") Instant since);
}
