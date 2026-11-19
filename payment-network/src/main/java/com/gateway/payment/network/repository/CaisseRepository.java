package com.gateway.payment.network.repository;

import com.gateway.payment.network.domain.Caisse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaisseRepository extends JpaRepository<Caisse, UUID> {

    boolean existsByPointVenteIdAndStatut(UUID pointVenteId, String statut);

    boolean existsByUtilisateurIdAndStatut(UUID utilisateurId, String statut);

    boolean existsByUtilisateurIdInAndStatut(Collection<UUID> utilisateurIds, String statut);

    Optional<Caisse> findFirstByUtilisateurIdInAndStatut(Collection<UUID> utilisateurIds, String statut);

    boolean existsByPointVenteIdAndUtilisateurIdAndStatut(UUID pointVenteId, UUID utilisateurId, String statut);

    Optional<Caisse> findFirstByUtilisateurIdAndStatut(UUID utilisateurId, String statut);

    List<Caisse> findByUtilisateurIdAndStatutOrderByOuverteLeDesc(UUID utilisateurId, String statut);

    List<Caisse> findTop40ByUtilisateurIdAndStatutNotOrderByOuverteLeDesc(UUID utilisateurId, String statut);

    List<Caisse> findByPointVenteIdInAndStatutIn(Collection<UUID> pointVenteIds, Collection<String> statuts);

    @Query("select coalesce(max(c.numero), 0) from Caisse c where c.pointVenteId = :pointVenteId")
    int maxNumero(@Param("pointVenteId") UUID pointVenteId);
}
