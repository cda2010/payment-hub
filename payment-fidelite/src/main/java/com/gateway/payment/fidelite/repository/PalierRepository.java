package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.Palier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PalierRepository extends JpaRepository<Palier, UUID> {

    List<Palier> findByProgramme_IdOrderBySeuilPointsAsc(UUID programmeId);

    List<Palier> findByProgramme_IdInOrderBySeuilPointsAsc(Collection<UUID> programmeIds);
}
