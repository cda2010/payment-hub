package com.gateway.payment.divers.repository;

import com.gateway.payment.divers.domain.Depense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DepenseRepository extends JpaRepository<Depense, UUID> {

    boolean existsByPaymentId(String paymentId);

    @Query("""
            select coalesce(sum(d.amount), 0)
            from Depense d
            where d.partyId = :partyId
              and d.categorie.code = :categorie
              and d.occurredAt >= :from
              and d.occurredAt < :to
            """)
    BigDecimal sumForCategory(
            @Param("partyId") UUID partyId,
            @Param("categorie") String categorie,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
            select coalesce(sum(d.amount), 0)
            from Depense d
            where d.partyId = :partyId
              and d.occurredAt >= :from
              and d.occurredAt < :to
            """)
    BigDecimal sumForParty(
            @Param("partyId") UUID partyId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
            select d.categorie.code, coalesce(sum(d.amount), 0)
            from Depense d
            where d.partyId = :partyId
              and d.occurredAt >= :from
              and d.occurredAt < :to
            group by d.categorie.code
            """)
    List<Object[]> sumByCategory(
            @Param("partyId") UUID partyId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
            select d.merchantName, coalesce(sum(d.amount), 0)
            from Depense d
            where d.partyId = :partyId
              and d.occurredAt >= :from
              and d.occurredAt < :to
              and d.merchantName is not null
            group by d.merchantName
            order by sum(d.amount) desc
            """)
    List<Object[]> topMerchants(
            @Param("partyId") UUID partyId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
