package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, UUID> {

    Optional<LoyaltyAccount> findByTenantIdAndProgramme_IdAndPartyId(String tenantId, UUID programmeId, UUID partyId);

    @Modifying(flushAutomatically = true)
    @Query("""
            update LoyaltyAccount a
            set a.balance = a.balance + :points, a.version = a.version + 1
            where a.id = :id and a.tenantId = :tenant
            """)
    int credit(@Param("id") UUID id, @Param("tenant") String tenant, @Param("points") long points);

    @Modifying(flushAutomatically = true)
    @Query("""
            update LoyaltyAccount a
            set a.reservedBalance = a.reservedBalance + :points, a.version = a.version + 1
            where a.id = :id and a.tenantId = :tenant
              and a.balance - a.reservedBalance >= :points
            """)
    int reserve(@Param("id") UUID id, @Param("tenant") String tenant, @Param("points") long points);

    @Modifying(flushAutomatically = true)
    @Query("""
            update LoyaltyAccount a
            set a.balance = a.balance - :points,
                a.reservedBalance = a.reservedBalance - :points,
                a.version = a.version + 1
            where a.id = :id and a.tenantId = :tenant
              and a.reservedBalance >= :points
              and a.balance >= :points
            """)
    int consume(@Param("id") UUID id, @Param("tenant") String tenant, @Param("points") long points);
}
