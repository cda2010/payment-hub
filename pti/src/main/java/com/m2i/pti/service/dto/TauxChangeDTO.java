package com.m2i.pti.service.dto;


import java.time.LocalDate;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import com.m2i.pti.domain.enumeration.TypeChange;

/**
 * A DTO for the TauxChange entity.
 */
public class TauxChangeDTO implements Serializable {

    private Long id;

    private LocalDate dateTaux;

    @NotNull
    private BigDecimal taux;

    private TypeChange typeChange;

    private Long deviseDepartId;

    private String deviseDepartLibelle;

    private Long deviseArriveeId;

    private String deviseArriveeLibelle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateTaux() {
        return dateTaux;
    }

    public void setDateTaux(LocalDate dateTaux) {
        this.dateTaux = dateTaux;
    }

    public BigDecimal getTaux() {
        return taux;
    }

    public void setTaux(BigDecimal taux) {
        this.taux = taux;
    }

    public TypeChange getTypeChange() {
        return typeChange;
    }

    public void setTypeChange(TypeChange typeChange) {
        this.typeChange = typeChange;
    }

    public Long getDeviseDepartId() {
        return deviseDepartId;
    }

    public void setDeviseDepartId(Long deviseId) {
        this.deviseDepartId = deviseId;
    }

    public String getDeviseDepartLibelle() {
        return deviseDepartLibelle;
    }

    public void setDeviseDepartLibelle(String deviseLibelle) {
        this.deviseDepartLibelle = deviseLibelle;
    }

    public Long getDeviseArriveeId() {
        return deviseArriveeId;
    }

    public void setDeviseArriveeId(Long deviseId) {
        this.deviseArriveeId = deviseId;
    }

    public String getDeviseArriveeLibelle() {
        return deviseArriveeLibelle;
    }

    public void setDeviseArriveeLibelle(String deviseLibelle) {
        this.deviseArriveeLibelle = deviseLibelle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TauxChangeDTO tauxChangeDTO = (TauxChangeDTO) o;
        if(tauxChangeDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), tauxChangeDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TauxChangeDTO{" +
            "id=" + getId() +
            ", dateTaux='" + getDateTaux() + "'" +
            ", taux='" + getTaux() + "'" +
            ", typeChange='" + getTypeChange() + "'" +
            "}";
    }
}
