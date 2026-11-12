package com.gateway.payment.network.repository;

import com.gateway.payment.network.domain.FamilleFse;
import com.gateway.payment.network.domain.StatutUnite;
import com.gateway.payment.network.domain.TypeUnite;
import com.gateway.payment.network.domain.UniteOrganisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UniteOrganisationRepository
        extends JpaRepository<UniteOrganisation, UUID>, JpaSpecificationExecutor<UniteOrganisation> {

    boolean existsByCode(String code);

    boolean existsByCodeFse(String codeFse);

    Optional<UniteOrganisation> findByCode(String code);

    Optional<UniteOrganisation> findByCodeAndType(String code, TypeUnite type);

    List<UniteOrganisation> findByTypeAndFamilleFseAndStatutAndCodeFseIsNotNullOrderByLibelle(
            TypeUnite type, FamilleFse famille, StatutUnite statut);

    List<UniteOrganisation> findByCodeIn(Collection<String> codes);

    /** Pays d'une zone : les racines de son arbre. */
    List<UniteOrganisation> findByZoneAndParentIdIsNullOrderByCode(String zone);

    Page<UniteOrganisation> findByParentId(UUID parentId, Pageable pageable);

    /** Nombre d'unités rattachées à chacun des parents donnés : [parentId, nombre]. */
    @Query("select u.parentId, count(u) from UniteOrganisation u where u.parentId in :parents group by u.parentId")
    List<Object[]> compterEnfants(Collection<UUID> parents);

    List<UniteOrganisation> findByParentIdOrderByCode(UUID parentId);

    long countByParentId(UUID parentId);

    boolean existsByParentIdAndStatut(UUID parentId, StatutUnite statut);

    long countByZone(String zone);

    long countByZoneAndType(String zone, TypeUnite type);

    boolean existsByZoneAndTypeAndStatut(String zone, TypeUnite type, StatutUnite statut);

    /**
     * Déplacement d'une branche : remplace le début du chemin et décale le niveau de toutes les unités dont le
     * chemin commence par {@code ancien} (l'unité déplacée comprise).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update UniteOrganisation u
               set u.chemin = concat(:nouveau, substring(u.chemin, :longueurAncien + 1)),
                   u.niveau = u.niveau + :decalage,
                   u.updatedAt = :maintenant,
                   u.updatedBy = :acteur
             where u.chemin like concat(:ancien, '%')
            """)
    int deplacerBranche(String ancien, int longueurAncien, String nouveau, int decalage,
                        Instant maintenant, String acteur);
}
