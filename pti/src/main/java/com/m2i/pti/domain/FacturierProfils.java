package com.m2i.pti.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.QueryHint;
import jakarta.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.m2i.pti.domain.enumeration.TypeActeurFacturier;


/**
 * A Facturier.
 */
 
@NamedNativeQueries({
    @NamedNativeQuery(name = "FacturierProfils.findAllFacteurProfil",
            query = "select fact.id as facturier_id,fact.nom_facturier,gpa.id as groupe_profil_acteur_id,gpa.nom,type_acteur FROM facturier_profil fact_pro inner join facturier fact ON fact.id = fact_pro.facturier_id inner join groupe_profil_acteur gpa ON gpa.id = fact_pro.groupe_profil_acteur_id order by fact.id desc",
            hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),  
            })
@Entity
@Table(name = "facturier_profil")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class FacturierProfils extends AbstractAuditingEntity implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Facturier facturier;

    @ManyToOne  
    private GroupeProfilActeur groupeProfilActeur;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_acteur")
    private TypeActeurFacturier typeActeur;

    public Facturier getFacturier() {
        return facturier;
    }

    public void setFacturier(Facturier facturier) {
        this.facturier = facturier;
    }

    public GroupeProfilActeur getGroupeProfilActeur() {
        return groupeProfilActeur;
    }

    public void setGroupeProfilActeur(GroupeProfilActeur groupeProfilActeur) {
        this.groupeProfilActeur = groupeProfilActeur;
    }

    public TypeActeurFacturier getTypeActeur() {
        return typeActeur;
    }

    public void setTypeActeur(TypeActeurFacturier typeActeur) {
        this.typeActeur = typeActeur;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    
}
