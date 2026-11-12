package com.gateway.payment.network.repository;

import com.gateway.payment.network.domain.RegroupementMembre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegroupementMembreRepository extends JpaRepository<RegroupementMembre, UUID> {

    List<RegroupementMembre> findByRegroupementId(UUID regroupementId);

    long countByRegroupementId(UUID regroupementId);

    Optional<RegroupementMembre> findByRegroupementIdAndUniteId(UUID regroupementId, UUID uniteId);

    List<RegroupementMembre> findByUniteIdIn(Collection<UUID> uniteIds);

    /** Appartenances des unités de la branche (chemin donné compris). */
    @Query("""
            select m from RegroupementMembre m, UniteOrganisation u
             where m.uniteId = u.id and u.chemin like concat(:chemin, '%')
            """)
    List<RegroupementMembre> findDansBranche(String chemin);

    List<RegroupementMembre> findByRegroupementIdIn(Collection<UUID> regroupementIds);

    /** Nombre de membres de chacun des regroupements donnés : [regroupementId, nombre]. */
    @Query("select m.regroupementId, count(m) from RegroupementMembre m where m.regroupementId in :regroupements group by m.regroupementId")
    List<Object[]> compterMembres(Collection<UUID> regroupements);
}
