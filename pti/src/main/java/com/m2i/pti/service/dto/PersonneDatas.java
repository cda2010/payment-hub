package com.m2i.pti.service.dto;

import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Personne;
import com.m2i.pti.domain.PersonneMorale;
import com.m2i.pti.domain.PersonnePhysique;
import com.m2i.pti.domain.Piece;

public class PersonneDatas {
    private PersonnePhysique personnePhysique;
    private Personne personne;
    private PersonneMorale personneMorale;
    private Piece piece;
    private Client client;

    public PersonnePhysique getPersonnePhysique() {
        return personnePhysique;
    }

    public void setPersonnePhysique(PersonnePhysique personnePhysique) {
        this.personnePhysique = personnePhysique;
    }

    public Personne getPersonne() {
        return personne;
    }

    public void setPersonne(Personne personne) {
        this.personne = personne;
    }

    public PersonneMorale getPersonneMorale() {
        return personneMorale;
    }

    public void setPersonneMorale(PersonneMorale personneMorale) {
        this.personneMorale = personneMorale;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
