package com.gateway.payment.network.repository;

import com.gateway.payment.network.domain.AffectationCaissier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AffectationCaissierRepository extends JpaRepository<AffectationCaissier, UUID> {

    List<AffectationCaissier> findByPointVenteIdOrderByDebutDesc(UUID pointVenteId);

    List<AffectationCaissier> findByPointVenteIdInOrderByDebutDesc(Collection<UUID> pointVenteIds);

    Optional<AffectationCaissier> findByPointVenteIdAndUtilisateurIdAndStatut(
            UUID pointVenteId, UUID utilisateurId, String statut);
}
