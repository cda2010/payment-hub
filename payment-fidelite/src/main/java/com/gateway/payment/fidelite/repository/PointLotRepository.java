package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.PointLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointLotRepository extends JpaRepository<PointLot, UUID> {

    @Query("""
            select l from PointLot l
            where l.account.id = :accountId and l.remainingPoints > 0
            order by case when l.expiresAt is null then 1 else 0 end, l.expiresAt, l.earnedAt
            """)
    List<PointLot> ouverts(@Param("accountId") UUID accountId);

    Optional<PointLot> findByTenantIdAndBusinessReference(String tenantId, String businessReference);

    @Query("""
            select l from PointLot l
            where l.remainingPoints > 0 and l.expiresAt is not null and l.expiresAt <= :instant
            """)
    List<PointLot> expires(@Param("instant") Instant instant);

    @Query("""
            select l from PointLot l
            where l.remainingPoints > 0 and l.expiresAt is not null
              and l.expiresAt > :debut and l.expiresAt <= :fin
            """)
    List<PointLot> expirantEntre(@Param("debut") Instant debut, @Param("fin") Instant fin);

    @Query("""
            select l.expiresAt, sum(l.remainingPoints)
            from PointLot l
            where l.account.programme.id = :programmeId and l.remainingPoints > 0
            group by l.expiresAt
            """)
    List<Object[]> passif(@Param("programmeId") UUID programmeId);
}
