package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.MouvementPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MouvementPointsRepository extends JpaRepository<MouvementPoints, UUID> {

    List<MouvementPoints> findByProgramme_IdAndPartyIdOrderByCreatedAtDesc(UUID programmeId, UUID partyId);

    @Query("""
            select coalesce(sum(m.points), 0)
            from MouvementPoints m
            where m.programme.id = :programmeId and m.partyId = :partyId
            """)
    int solde(@Param("programmeId") UUID programmeId, @Param("partyId") UUID partyId);
}
