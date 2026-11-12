package com.m2i.pti.service.dto;


import java.time.ZonedDateTime;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the ReleveCompte entity.
 */
public class ReleveCompteDTO implements Serializable {

    private Long id;

    private BigDecimal soldeInitial;

    private ZonedDateTime dateDebut;

    private BigDecimal soldeFinal;

    private ZonedDateTime dateFin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getSoldeInitial() {
        return soldeInitial;
    }

    public void setSoldeInitial(BigDecimal soldeInitial) {
        this.soldeInitial = soldeInitial;
    }

    public ZonedDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(ZonedDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public BigDecimal getSoldeFinal() {
        return soldeFinal;
    }

    public void setSoldeFinal(BigDecimal soldeFinal) {
        this.soldeFinal = soldeFinal;
    }

    public ZonedDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(ZonedDateTime dateFin) {
        this.dateFin = dateFin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReleveCompteDTO releveCompteDTO = (ReleveCompteDTO) o;
        if(releveCompteDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), releveCompteDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ReleveCompteDTO{" +
            "id=" + getId() +
            ", soldeInitial='" + getSoldeInitial() + "'" +
            ", dateDebut='" + getDateDebut() + "'" +
            ", soldeFinal='" + getSoldeFinal() + "'" +
            ", dateFin='" + getDateFin() + "'" +
            "}";
    }
}
