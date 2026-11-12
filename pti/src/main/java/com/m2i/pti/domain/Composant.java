package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.TypeComposant;

/**
 * A Composant.
 */
@Entity
@Table(name = "composant")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Composant extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jhi_type")
    private String type;

    @Column(name = "jhi_label")
    private String label;

    @Column(name = "code")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_composant")
    private TypeComposant typeComposant;

    @OneToOne
    private Liste liste;

    @OneToOne
    private Bouton bouton;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "attribut")
    private String attribut;

    @Column(name ="select_multiple")
    private Integer selectMultiple;

    @OneToMany(mappedBy = "composant")
    @JsonIgnore
 
    private Set<Champs> champs = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public Composant type(String type) {
        this.type = type;
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public Composant label(String label) {
        this.label = label;
        return this;
    }

    public Liste getListe() {
        return liste;
    }

    public void setListe(Liste liste) {
        this.liste = liste;
    }

    public Bouton getBouton() {
        return bouton;
    }

    public void setBouton(Bouton bouton) {
        this.bouton = bouton;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public Composant code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public TypeComposant getTypeComposant() {
        return typeComposant;
    }

    public Composant typeComposant(TypeComposant typeComposant) {
        this.typeComposant = typeComposant;
        return this;
    }

    public Set<Champs> getChamps() {
        return champs;
    }

    public Composant champs(Set<Champs> champs) {
        this.champs = champs;
        return this;
    }

    public Composant addChamps(Champs champs) {
        this.champs.add(champs);
        champs.setComposant(this);
        return this;
    }

    public Composant removeChamps(Champs champs) {
        this.champs.remove(champs);
        champs.setComposant(null);
        return this;
    }

    public void setTypeComposant(TypeComposant typeComposant) {
        this.typeComposant = typeComposant;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getAttribut() {
        return attribut;
    }

    public void setAttribut(String attribut) {
        this.attribut = attribut;
    }

    public void setChamps(Set<Champs> champs) {
        this.champs = champs;
    }

    public Integer getSelectMultiple() {
        return selectMultiple;
    }

    public void setSelectMultiple(Integer selectMultiple) {
        this.selectMultiple = selectMultiple;
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
        Composant composant = (Composant) o;
        if (composant.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), composant.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }



}
