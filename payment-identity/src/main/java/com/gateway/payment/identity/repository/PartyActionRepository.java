package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.PartyAction;
import com.gateway.payment.identity.domain.PartyActionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartyActionRepository extends JpaRepository<PartyAction, UUID> {

    @Query("""
            select a from PartyAction a
            join fetch a.type t
            where a.partyId = :partyId
              and a.status = :status
            order by t.priority asc, a.createdAt asc
            """)
    List<PartyAction> findOpenByParty(
            @Param("partyId") UUID partyId,
            @Param("status") PartyActionStatus status
    );

    @Query("""
            select a from PartyAction a
            join fetch a.type t
            where a.partyId = :partyId
              and t.code = :actionType
              and a.status = com.gateway.payment.identity.domain.PartyActionStatus.OPEN
              and coalesce(a.aggregateType, '') = coalesce(:aggregateType, '')
              and coalesce(a.aggregateId, '') = coalesce(:aggregateId, '')
            """)
    Optional<PartyAction> findOpen(
            @Param("partyId") UUID partyId,
            @Param("actionType") String actionType,
            @Param("aggregateType") String aggregateType,
            @Param("aggregateId") String aggregateId
    );

    @Query("""
            select a from PartyAction a
            join fetch a.type t
            where a.id = :id and a.partyId = :partyId
            """)
    Optional<PartyAction> findByIdAndPartyId(@Param("id") UUID id, @Param("partyId") UUID partyId);

    @Query("""
            select a from PartyAction a
            where a.status = com.gateway.payment.identity.domain.PartyActionStatus.OPEN
              and a.expiresAt is not null
              and a.expiresAt < :now
            """)
    List<PartyAction> findExpiredOpen(@Param("now") Instant now);
}
