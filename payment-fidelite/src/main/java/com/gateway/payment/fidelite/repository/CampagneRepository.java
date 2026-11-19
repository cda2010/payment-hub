package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.Campagne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampagneRepository extends JpaRepository<Campagne, UUID> {

    List<Campagne> findByProgramme_IdOrderByPrioriteDesc(UUID programmeId);

    Optional<Campagne> findByIdAndProgramme_Id(UUID id, UUID programmeId);

    @Query("""
            select c from Campagne c
            where c.programme.id = :programmeId and c.actif = true
              and c.debut <= :instant and c.fin > :instant
            order by c.priorite desc
            """)
    List<Campagne> ouvertes(@Param("programmeId") UUID programmeId, @Param("instant") Instant instant);

    @Modifying(flushAutomatically = true)
    @Query("""
            update Campagne c
            set c.pointsAttribues = c.pointsAttribues + :points
            where c.id = :id
              and (c.budgetPoints is null or c.pointsAttribues + :points <= c.budgetPoints)
            """)
    int attribuer(@Param("id") UUID id, @Param("points") long points);
}
