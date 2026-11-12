package com.m2i.pti.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the CompteBanque entity.
 */
public class CompteBanqueDTO implements Serializable {

    private Long id;

    @NotNull
    private String codeAgence;

    @NotNull
    private String codeBanque;

    private String codeFse;

    private Long deviseId;

    private String deviseCode;

    private String rib;
    private String alias;

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getCodeBanque() {
        return codeBanque;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public Long getDeviseId() {
        return deviseId;
    }

    public void setDeviseId(Long deviseId) {
        this.deviseId = deviseId;
    }

    public String getDeviseCode() {
        return deviseCode;
    }

    public void setDeviseCode(String deviseCode) {
        this.deviseCode = deviseCode;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompteBanqueDTO compteBanqueDTO = (CompteBanqueDTO) o;
        if (compteBanqueDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), compteBanqueDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CompteBanqueDTO{"
                + "id=" + getId()
                + ", codeAgence='" + getCodeAgence() + "'"
                + ", codeBanque='" + getCodeBanque() + "'"
                + ", codeFse='" + getCodeFse() + "'"
                + "}";
    }
}
