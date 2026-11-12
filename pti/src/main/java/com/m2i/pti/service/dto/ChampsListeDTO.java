package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.TypeComposant;

public class ChampsListeDTO {

    private Long id;
    private Integer aSaisir;
    private Integer min;
    private Integer max;
    private Integer minLength;
    private Integer maxLength;
    private Integer isVisible;
    private Integer obligatoire;
    private String pattern;
    private String label;
    private String defaultValue;
    private Long composantId;
    private String composantLabel;
    private String composantType;
    private String typeComposant;

    public ChampsListeDTO(Long id, Integer aSaisir, Integer min, Integer max, Integer minLength, Integer maxLength, Integer isVisible, Integer obligatoire, String pattern, String label, String defaultValue, Long composantId, String composantLabel, String composantType, TypeComposant typeComposant) {
        this.id = id;
        this.aSaisir = aSaisir;
        this.min = min;
        this.max = max;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.isVisible = isVisible;
        this.obligatoire = obligatoire;
        this.pattern = pattern;
        this.label = label;
        this.defaultValue = defaultValue;
        this.composantId = composantId;
        this.composantLabel = composantLabel;
        this.composantType = composantType;
        this.typeComposant = typeComposant == null ? null : typeComposant.name();
    }

    public Long getId() {
        return id;
    }

    public Integer getASaisir() {
        return aSaisir;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public Integer getIsVisible() {
        return isVisible;
    }

    public Integer getObligatoire() {
        return obligatoire;
    }

    public String getPattern() {
        return pattern;
    }

    public String getLabel() {
        return label;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Long getComposantId() {
        return composantId;
    }

    public String getComposantLabel() {
        return composantLabel;
    }

    public String getComposantType() {
        return composantType;
    }

    public String getTypeComposant() {
        return typeComposant;
    }
}
