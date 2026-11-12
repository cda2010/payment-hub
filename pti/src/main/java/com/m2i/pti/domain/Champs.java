package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.domain.enumeration.TypeComposant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Champs.
 */
@Entity
@Table(name = "champs")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable(value = true)
public class Champs extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "a_saisir")
    private Integer aSaisir;

    @Column(name = "jhi_min")
    private Integer min;

    @Column(name = "jhi_max")
    private Integer max;

    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "attribut")
    private String attribut;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "is_visible")
    private Integer isVisible;

    @Column(name = "pattern")
    private String pattern;

    @Column(name = "jhi_type")
    private String type;

    @Column(name = "jhi_label")
    private String label;

    @Column(name = "code")
    private String code;
    @Column(name = "destination")
    private String destination;
    @Column(name = "obligatoire")
    private Integer obligatoire;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_composant")
    private TypeComposant typeComposant;

    @OneToOne
    private Liste liste;

    @OneToOne
    private SelectComponentData selectComponentData;

    @OneToOne
    private Bouton bouton;

    @ManyToOne
//    @JsonIgnore
    private Ecran ecran;

    @ManyToOne
    private Composant composant;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getaSaisir() {
        return aSaisir;
    }

    public Champs aSaisir(Integer aSaisir) {
        this.aSaisir = aSaisir;
        return this;
    }

    public void setaSaisir(Integer aSaisir) {
        this.aSaisir = aSaisir;
    }

    public Integer getMin() {
        return min;
    }

    public Champs min(Integer min) {
        this.min = min;
        return this;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public Champs max(Integer max) {
        this.max = max;
        return this;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public Champs minLength(Integer minLength) {
        this.minLength = minLength;
        return this;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public Champs maxLength(Integer maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getIsVisible() {
        return isVisible;
    }

    public Champs isVisible(Integer isVisible) {
        this.isVisible = isVisible;
        return this;
    }

    public void setIsVisible(Integer isVisible) {
        this.isVisible = isVisible;
    }

    public String getPattern() {
        return pattern;
    }

    public Champs pattern(String pattern) {
        this.pattern = pattern;
        return this;
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

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Ecran getEcran() {
        return ecran;
    }

    public Champs ecran(Ecran ecran) {
        this.ecran = ecran;
        return this;
    }

    public void setEcran(Ecran ecran) {
        this.ecran = ecran;
    }

    public Composant getComposant() {
        return composant;
    }

    public Champs composant(Composant composant) {
        this.composant = composant;
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public TypeComposant getTypeComposant() {
        return typeComposant;
    }

    public void setTypeComposant(TypeComposant typeComposant) {
        this.typeComposant = typeComposant;
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

    public Integer getObligatoire() {
        return obligatoire;
    }

    public void setObligatoire(Integer obligatoire) {
        this.obligatoire = obligatoire;
    }

    public void setComposant(Composant composant) {
        this.composant = composant;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public SelectComponentData getSelectComponentData() {
        return selectComponentData;
    }

    public void setSelectComponentData(SelectComponentData selectComponentData) {
        this.selectComponentData = selectComponentData;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Champs champs = (Champs) o;
        if (champs.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), champs.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

}
