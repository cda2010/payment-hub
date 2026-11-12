package com.m2i.pti.reporting.util;

import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.enumeration.EtatCompte;
import com.m2i.pti.domain.enumeration.StatutCompte;

public class UtilService {

    public static String nameToClient(Client client){
       String name = client.getPersonne().getPersonneMorale() != null ? client.getPersonne().getPersonneMorale().getRaisonSociale() :
               client.getPersonne().getPersonnePhysique().getNomClient() + " " + client.getPersonne().getPersonnePhysique().getPrenomClient();

       return name;
    }

    public static String etatBloqueOuCloture(Compte compte){
        if (compte.getStatutCompte().equals(StatutCompte.DESACTIVE))
            return "Bloqué";
        else if (compte.getEtatCompte().equals(EtatCompte.FERME))
            return "Clôturé";
        else
            return null;
    }

    public static String nameToClient(Compte compte){

        if (compte.getClient() != null){
            return compte.getClient().getPersonne().getPersonneMorale() != null ? compte.getClient().getPersonne().getPersonneMorale().getRaisonSociale() :
                    compte.getClient().getPersonne().getPersonnePhysique().getNomClient() + " " + compte.getClient().getPersonne().getPersonnePhysique().getPrenomClient();
        }
        else {
            return compte.getUniteOrganisation().getLibelleUniteOrganisation();
        }
    }

    public static String formatDureInYearMoisJour(long nombreJour){
        long nombreAnnee = nombreJour/360;
        long nombreMois = (nombreJour%360)/30;
        long nombreJours = ((nombreJour%360))%30;

        String annee = "";
        String mois = "";
        String jour = "";

        if (nombreAnnee == 1)
            annee = nombreAnnee+" an ";
        if (nombreAnnee > 1)
            annee = nombreAnnee+" ans ";
        if (nombreMois >= 1)
            mois = nombreMois+" mois ";
        if (nombreJours == 1)
            jour = nombreJours+" jour";
        if (nombreJours > 1)
            jour = nombreJours+" jours";

        return annee.concat(mois.concat(jour));

    }
}
