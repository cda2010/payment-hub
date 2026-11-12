package com.pti.emeraude.gateway.domain;

;
import java.util.Objects;

/**
 * A Champs.
 */


public class MChamps {

    private Long id;
    private Integer aSaisir;
    private Integer min;
    private Integer max;
    private Integer minLength;
    private Integer maxLength;
    private Integer isVisible;
    private String pattern;
    private String type;
    private String label;
    private String code;
    private String typeComposant;
    private String action;
    private Integer choixmultiple;
    private Integer obligatoire;
    private String options;
    private String defaultValue;
    private String destination;
    private Integer isStatic;
    private String listeDataToGet;

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

    public void setaSaisir(Integer aSaisir) {
        this.aSaisir = aSaisir;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Integer isVisible) {
        this.isVisible = isVisible;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
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

    public String getTypeComposant() {
        return typeComposant;
    }

    public void setTypeComposant(String typeComposant) {
        this.typeComposant = typeComposant;
    }

    public String getAction() {
        return action;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove
    public void setAction(String action) {
        this.action = action;
    }

    public Integer getChoixmultiple() {
        return choixmultiple;
    }

    public void setChoixmultiple(Integer choixmultiple) {
        this.choixmultiple = choixmultiple;
    }

    public Integer getObligatoire() {
        return obligatoire;
    }

    public void setObligatoire(Integer obligatoire) {
        this.obligatoire = obligatoire;
    }

    public Integer getIsStatic() {
        return isStatic;
    }

    public void setIsStatic(Integer isStatic) {
        this.isStatic = isStatic;
    }

    public String getListeDataToGet() {
        return listeDataToGet;
    }

    public void setListeDataToGet(String listeDataToGet) {
        this.listeDataToGet = listeDataToGet;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

}
