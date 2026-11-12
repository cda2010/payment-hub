package com.gateway.payment.payment.repository;

import com.gateway.payment.payment.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gateway.payment.common.domain.PaymentStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, UUID>, JpaSpecificationExecutor<PaymentTransaction> {

    Optional<PaymentTransaction> findByPaymentId(String paymentId);

    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentTransaction> findByProviderReference(String providerReference);

    @Query(value = """
            SELECT * FROM payment_transaction t
            WHERE t.provider_reference = :ref
               OR t.payment_id = :ref
               OR (t.context_json IS NOT NULL AND (
                    t.context_json::jsonb->>'remittanceReference' = :ref
                 OR t.context_json::jsonb->>'quoteId' = :ref
               ))
            ORDER BY t.created_at DESC
            LIMIT 20
            """, nativeQuery = true)
    List<PaymentTransaction> findRemittanceCandidatesByReference(@Param("ref") String ref);

    @Query("""
            SELECT t.status, COUNT(t)
            FROM PaymentTransaction t
            WHERE t.createdAt >= :startOfDay
            GROUP BY t.status
            """)
    List<Object[]> countByStatusSince(@Param("startOfDay") Instant startOfDay);

    @Query("""
            SELECT t.status, COUNT(t)
            FROM PaymentTransaction t
            WHERE t.createdAt >= :startOfDay
              AND (:tenantId IS NULL OR t.tenantId = :tenantId)
            GROUP BY t.status
            """)
    List<Object[]> countByStatusSinceForTenant(
            @Param("startOfDay") Instant startOfDay,
            @Param("tenantId") String tenantId);

    List<PaymentTransaction> findByStatusAndUpdatedAtBefore(PaymentStatus status, Instant updatedBefore);
}
