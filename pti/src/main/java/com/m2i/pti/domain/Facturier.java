package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.external.domain.ExFacturier;
import com.m2i.pti.service.dto.FacturierDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.TypeFacturier;

import com.m2i.pti.domain.enumeration.SecteurActivite;

/**
 * A Facturier.
 */
@NamedNativeQueries({
    @NamedNativeQuery(name = "Facturier.findByCodePosseurAndSecteur",
            query = "select fact.id, uo1.code_uo codeUo, code_facturier codeFacturier, nom_facturier nomFacturier, valeur image ,valeur_content_type  valeurContentType from facturier fact, unite_organisation uo , unite_organisation uo1 where activated=true "
            + " and fact.possesseur_id=uo.id and uo.code_uo=:codeUo and secteur_activite in :secteur  and uo1.id=fact.unite_organisation_id",
            resultSetMapping = "Mapping.FactuierDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
    @NamedNativeQuery(name = "Facturier.findAllFacturierOptimiser",
            query = "select f.id, nom_facturier as nomFacturier,code_facturier as codeFacturier,type_facturier as typeFacturierLibelle,c.libelle_classification as classificationLibelle,poc.libelle_unite_organisation as possesseurLibelle,\n"
            + "u.libelle_unite_organisation as marchand,fse,secteur_activite as secteurActiviteLibelle,f.activated from facturier f left join classification_facturier c\n"
            + "on f.classification_id=c.id left join unite_organisation poc on f.possesseur_id=poc.id left join unite_organisation u on\n"
            + "f.unite_organisation_id=u.id order by f.id limit :taille offset :page ",
            resultSetMapping = "Mapping.FacturierDTONew", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),
    @NamedNativeQuery(name = "Facturier.findAllFacturierOptimiser2",
            query = "select f.id, nom_facturier as nomFacturier,code_facturier as codeFacturier,type_facturier as typeFacturier ,c.id as classificationId,c.libelle_classification as classificationLibelle,poc.id as possesseurId,poc.libelle_unite_organisation as possesseurLibelle,\n"
                    + "u.id as marchandId ,u.libelle_unite_organisation as marchand,fse,secteur_activite as secteurActivite ,f.activated from facturier f left join classification_facturier c\n"
                    + "on f.classification_id=c.id left join unite_organisation poc on f.possesseur_id=poc.id left join unite_organisation u on\n"
                    + "f.unite_organisation_id=u.id order by f.id limit :taille offset :page ",
            resultSetMapping = "Mapping.FacturierDTONew", hints = {
            @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
    @NamedNativeQuery(name = "Facturier.findByCodePosseuAndSecteurUo",
            query = "select fact.id, uo1.code_uo codeUo, code_facturier codeFacturier, nom_facturier nomFacturier, valeur image ,valeur_content_type  valeurContentType from facturier fact, unite_organisation uo , unite_organisation uo1 where activated=true "
            + " and fact.possesseur_id=uo.id  and secteur_activite in :secteur  and uo1.id=fact.unite_organisation_id and uo1.code_uo=:codeUo",
            resultSetMapping = "Mapping.FactuierDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
})
@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.FactuierDTO",
            classes = @ConstructorResult(targetClass = ExFacturier.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                                @ColumnResult(name = "codeUo", type = String.class)
                        ,
                                @ColumnResult(name = "codeFacturier", type = String.class)
                        ,
                                @ColumnResult(name = "nomFacturier", type = String.class)
                        ,
                                @ColumnResult(name = "image", type = byte[].class)
                        ,
                                @ColumnResult(name = "valeurContentType", type = String.class)

                    }))
    ,
        @SqlResultSetMapping(name = "Mapping.FacturierDTONew",
    classes = @ConstructorResult(targetClass = FacturierDTO.class,
            columns = {
                @ColumnResult(name = "id", type = Long.class)
                ,
                        @ColumnResult(name = "nomFacturier", type = String.class)
                ,
                        @ColumnResult(name = "codeFacturier", type = String.class)
                ,
                        @ColumnResult(name = "typeFacturier", type = String.class)
                ,
                    @ColumnResult(name = "classificationId", type = Long.class)
                    ,
                        @ColumnResult(name = "classificationLibelle", type = String.class),
                    @ColumnResult(name = "possesseurId", type = Long.class)
                    ,
                        @ColumnResult(name = "possesseurLibelle", type = String.class)
                ,  @ColumnResult(name = "marchandId", type = Long.class)
                    ,

                        @ColumnResult(name = "marchand", type = String.class)
                ,
                        @ColumnResult(name = "fse", type = String.class)
                ,
                        @ColumnResult(name = "secteurActivite", type = String.class)
                ,
                        @ColumnResult(name = "activated", type = boolean.class)

            }))

})

@Entity
@Table(name = "facturier")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Facturier extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_facturier")
    private String nomFacturier;

    @Column(name = "code_facturier")
    private String codeFacturier;
    @Column
    private boolean activated = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_facturier")
    private TypeFacturier typeFacturier;

    @Enumerated(EnumType.STRING)
    @Column(name = "secteur_activite")
    private SecteurActivite secteurActivite;

    @ManyToOne
    private UniteOrganisation possesseur;

    @ManyToOne
    private ClassificationFacturier classification;

    @ManyToOne
    private UniteOrganisation uniteOrganisation;

    @Lob
    @Column(name = "valeur")
    private byte[] valeur;

    @Column(name = "valeur_content_type")
    private String valeurContentType;

    private String fse;

    public Facturier(Long id, String nomFacturier) {
        this.id = id;
        this.nomFacturier = nomFacturier;
    }

    public Facturier() {
    }

    public SecteurActivite getSecteurActivite() {
        return secteurActivite;
    }

    public void setSecteurActivite(SecteurActivite secteurActivite) {
        this.secteurActivite = secteurActivite;
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomFacturier() {
        return nomFacturier;
    }

    public Facturier nomFacturier(String nomFacturier) {
        this.nomFacturier = nomFacturier;
        return this;
    }

    public String getFse() {
        return fse;
    }

    public void setFse(String fse) {
        this.fse = fse;
    }

    public void setNomFacturier(String nomFacturier) {
        this.nomFacturier = nomFacturier;
    }

    public String getCodeFacturier() {
        return codeFacturier;
    }

    public Facturier codeFacturier(String codeFacturier) {
        this.codeFacturier = codeFacturier;
        return this;
    }

    public void setCodeFacturier(String codeFacturier) {
        this.codeFacturier = codeFacturier;
    }

    public TypeFacturier getTypeFacturier() {
        return typeFacturier;
    }

    public Facturier typeFacturier(TypeFacturier typeFacturier) {
        this.typeFacturier = typeFacturier;
        return this;
    }

    public void setTypeFacturier(TypeFacturier typeFacturier) {
        this.typeFacturier = typeFacturier;
    }

    public UniteOrganisation getPossesseur() {
        return possesseur;
    }

    public Facturier possesseur(UniteOrganisation possesseur) {
        this.possesseur = possesseur;
        return this;
    }

    public void setPossesseur(UniteOrganisation possesseur) {
        this.possesseur = possesseur;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public Facturier uniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
        return this;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }

    public ClassificationFacturier getClassification() {
        return classification;
    }

    public void setClassification(ClassificationFacturier classification) {
        this.classification = classification;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public byte[] getValeur() {
        return valeur;
    }

    public void setValeur(byte[] valeur) {
        this.valeur = valeur;
    }

    public String getValeurContentType() {
        return valeurContentType;
    }

    public void setValeurContentType(String valeurContentType) {
        this.valeurContentType = valeurContentType;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Facturier facturier = (Facturier) o;
        if (facturier.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), facturier.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Facturier{"
                + "id=" + getId()
                + ", nomFacturier='" + getNomFacturier() + "'"
                + ", codeFacturier='" + getCodeFacturier() + "'"
                + ", typeFacturier='" + getTypeFacturier() + "'"
                + ", possesseur='" + getPossesseur() + "'"
                + ", uniteOrganisation='" + getUniteOrganisation() + "'"
                + ",image='" + Arrays.toString(getValeur()) + "'"
                + ",contentType='" + getValeurContentType() + "'"
                + "}";
    }

}
