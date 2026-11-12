package com.gateway.payment.pricing.tarif.moteur;

import com.gateway.payment.pricing.tarif.moteur.ResultatFrais.Correspondance;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Grille qu'appliquerait aujourd'hui le moteur à une source et une destination, sans montant.
 *
 * @param grille              la grille retenue (TROUVEE) ; vide sinon
 * @param autres              TROUVEE : les grilles moins précises qui visent aussi l'opération (5 au plus) ;
 *                            AMBIGUE : les grilles à égalité
 * @param voisinesSource      grilles publiées qui visent en source une autre unité du même type sous le même parent
 * @param voisinesDestination idem en destination
 */
public record GrilleApplicable(Statut statut, String message, Retenue grille, List<Retenue> autres,
                               long voisinesSource, long voisinesDestination) {

    public enum Statut { TROUVEE, AUCUNE, AMBIGUE }

    public record Retenue(UUID id, String code, int version, String libelle, String canal, LocalDate dateDebut,
                          LocalDate dateFin, Correspondance source, Correspondance destination) {
    }
}
