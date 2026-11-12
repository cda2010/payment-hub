package com.gateway.payment.pricing.tarif.repository;

import com.gateway.payment.pricing.tarif.domain.GrilleTarifaire;
import com.gateway.payment.pricing.tarif.domain.StatutGrille;
import com.gateway.payment.pricing.tarif.reseau.PositionReseau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GrilleTarifaireRepository
        extends JpaRepository<GrilleTarifaire, UUID>, JpaSpecificationExecutor<GrilleTarifaire> {

    /** Grilles publiées valides à la date, pour le canal demandé ou pour tous les canaux (canal vide). */
    String APPLICABLES = """
            g.statut = com.gateway.payment.pricing.tarif.domain.StatutGrille.PUBLIEE
              and g.produit = :produit
              and g.devise = :devise
              and (g.canal is null or g.canal = :canal)
              and g.dateDebut <= :date
              and (g.dateFin is null or g.dateFin >= :date)
            """;

    /**
     * Ce qu'un côté de l'opération peut viser : rien (côté inconnu : seules les grilles « toutes » passent), ou sa
     * zone, les unités de sa lignée et les regroupements qui la couvrent.
     */
    record Perimetre(boolean connu, String zone, List<UUID> ids) {

        public static final Perimetre INCONNU = new Perimetre(false, null, List.of());

        /** Identifiant qui ne correspond à aucune unité : une liste IN vide n'est pas portable. */
        private static final UUID AUCUN = new UUID(0, 0);

        public static Perimetre de(PositionReseau p) {
            if (p == null) {
                return INCONNU;
            }
            List<UUID> ids = new ArrayList<>();
            p.lignee().forEach(u -> ids.add(u.id()));
            p.regroupements().forEach(c -> ids.add(c.regroupement().id()));
            return new Perimetre(true, p.zone(), ids);
        }

        List<UUID> idsOuAucun() {
            return ids.isEmpty() ? List.of(AUCUN) : ids;
        }
    }

    @Query("select count(g) from GrilleTarifaire g where " + APPLICABLES)
    long compterApplicables(String produit, String canal, String devise, LocalDate date);

    @Query("select count(g) from GrilleTarifaire g where " + APPLICABLES + " and g.source.zone is not null")
    long compterCibleesSource(String produit, String canal, String devise, LocalDate date);

    @Query("select count(g) from GrilleTarifaire g where " + APPLICABLES + " and g.destination.zone is not null")
    long compterCibleesDestination(String produit, String canal, String devise, LocalDate date);

    /**
     * Grilles applicables qui peuvent viser l'opération : de chaque côté, « toutes », la zone, une unité de la lignée
     * ou un regroupement qui la couvre. Quelques grilles au plus, quel que soit le nombre de grilles ciblées.
     */
    @Query("select g from GrilleTarifaire g where " + APPLICABLES + """
              and (g.source.zone is null
                   or (:sourceConnue = true and ((g.source.uniteId is null and g.source.zone = :sourceZone)
                                                 or g.source.uniteId in :sourceIds)))
              and (g.destination.zone is null
                   or (:destinationConnue = true and ((g.destination.uniteId is null and g.destination.zone = :destinationZone)
                                                      or g.destination.uniteId in :destinationIds)))
            """)
    List<GrilleTarifaire> findCandidates(String produit, String canal, String devise, LocalDate date,
                                         boolean sourceConnue, String sourceZone, Collection<UUID> sourceIds,
                                         boolean destinationConnue, String destinationZone,
                                         Collection<UUID> destinationIds);

    default List<GrilleTarifaire> candidates(String produit, String canal, String devise, LocalDate date,
                                             Perimetre source, Perimetre destination) {
        return findCandidates(produit, canal, devise, date,
                source.connu(), source.zone(), source.idsOuAucun(),
                destination.connu(), destination.zone(), destination.idsOuAucun());
    }

    /** Grilles publiées en vigueur qui visent en source une autre unité du même type sous le même parent. */
    @Query("""
            select count(g) from GrilleTarifaire g
            where g.statut = com.gateway.payment.pricing.tarif.domain.StatutGrille.PUBLIEE
              and g.produit = :produit and g.devise = :devise
              and g.dateDebut <= :date and (g.dateFin is null or g.dateFin >= :date)
              and g.source.uniteParentId = :parentId and g.source.uniteType = :type and g.source.uniteId <> :uniteId
            """)
    long compterVoisinesSource(String produit, String devise, LocalDate date, UUID parentId, String type, UUID uniteId);

    /** Idem en destination. */
    @Query("""
            select count(g) from GrilleTarifaire g
            where g.statut = com.gateway.payment.pricing.tarif.domain.StatutGrille.PUBLIEE
              and g.produit = :produit and g.devise = :devise
              and g.dateDebut <= :date and (g.dateFin is null or g.dateFin >= :date)
              and g.destination.uniteParentId = :parentId and g.destination.uniteType = :type
              and g.destination.uniteId <> :uniteId
            """)
    long compterVoisinesDestination(String produit, String devise, LocalDate date, UUID parentId, String type,
                                    UUID uniteId);

    boolean existsByCode(String code);

    Optional<GrilleTarifaire> findFirstByCodeOrderByVersionDesc(String code);

    List<GrilleTarifaire> findByCodeAndStatut(String code, StatutGrille statut);

    List<GrilleTarifaire> findByProduitAndDeviseAndStatut(String produit, String devise, StatutGrille statut);
}
