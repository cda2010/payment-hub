package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.m2i.pti.domain.enumeration.TypeProfilActeur;
import com.m2i.pti.domain.util.JsonLigneGroupeProfilTemplate;


import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name="historique_groupe_profil_acteur")
public class HistoriqueGroupeProfilActeur extends  AbstractAuditingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_acteur")
    private TypeProfilActeur type;

    @ManyToOne
    private ProfilActeur profilActeur ;

    @JsonProperty("jsonTableauFrais")
    @Convert(converter = JsonLigneGroupeProfilTemplate.class)
    private LigneGroupeProfilTemplate jsonTableauFrais;

    @JsonProperty("jsonTableauCumul")
    @Convert(converter = JsonLigneGroupeProfilTemplate.class)
    private LigneGroupeProfilTemplate jsonTableauCumul;

    @JsonProperty("jsonGroupeRepartition")
    @Convert(converter = JsonLigneGroupeProfilTemplate.class)
    private LigneGroupeProfilTemplate jsonGroupeRepartition;

    @JsonProperty("jsonRepartitionCom")
    @Convert(converter = JsonLigneGroupeProfilTemplate.class)
    private LigneGroupeProfilTemplate jsonRepartitionCom;

    @JsonProperty("jsonHoraire")
    @Convert(converter = JsonLigneGroupeProfilTemplate.class)
    private LigneGroupeProfilTemplate jsonHoraire;

    public ProfilActeur getProfilActeur() {
        return profilActeur;
    }

    public LigneGroupeProfilTemplate getJsonGroupeRepartition() {
        return jsonGroupeRepartition;
    }

    public LigneGroupeProfilTemplate getJsonRepartitionCom() {
        return jsonRepartitionCom;
    }

    public LigneGroupeProfilTemplate getJsonTableauCumul() {
        return jsonTableauCumul;
    }

    public TypeProfilActeur getType() {
        return type;
    }

    public LigneGroupeProfilTemplate getJsonTableauFrais() {
        return jsonTableauFrais;
    }

    public void setType(TypeProfilActeur type) {
        this.type = type;
    }

    public void setJsonGroupeRepartition(LigneGroupeProfilTemplate jsonGroupeRepartition) {
        this.jsonGroupeRepartition = jsonGroupeRepartition;
    }

    public void setJsonRepartitionCom(LigneGroupeProfilTemplate jsonRepartitionCom) {
        this.jsonRepartitionCom = jsonRepartitionCom;
    }

    public void setJsonTableauCumul(LigneGroupeProfilTemplate jsonTableauCumul) {
        this.jsonTableauCumul = jsonTableauCumul;
    }

    public void setJsonTableauFrais(LigneGroupeProfilTemplate jsonTableauFrais) {
        this.jsonTableauFrais = jsonTableauFrais;
    }

    public LigneGroupeProfilTemplate getJsonHoraire() {
        return jsonHoraire;
    }

    public void setJsonHoraire(LigneGroupeProfilTemplate jsonHoraire) {
        this.jsonHoraire = jsonHoraire;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        super.setCreatedBy(createdBy);
    }

    public void setProfilActeur(ProfilActeur profilActeur) {
        this.profilActeur = profilActeur;
    }
}
