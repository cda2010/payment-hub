package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the RepartionFraisEmAcq entity.
 */
public class RepartionFraisEmAcqDTO implements Serializable {

    private Long id;

    private Integer partEm;

    private Integer partAcq;

    private Long typeServiceId;

    private String typeServiceLibelle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPartEm() {
        return partEm;
    }

    public void setPartEm(Integer partEm) {
        this.partEm = partEm;
    }

    public Integer getPartAcq() {
        return partAcq;
    }

    public void setPartAcq(Integer partAcq) {
        this.partAcq = partAcq;
    }

    public Long getTypeServiceId() {
        return typeServiceId;
    }

    public void setTypeServiceId(Long typeServiceId) {
        this.typeServiceId = typeServiceId;
    }

    public String getTypeServiceLibelle() {
        return typeServiceLibelle;
    }

    public void setTypeServiceLibelle(String typeServiceLibelle) {
        this.typeServiceLibelle = typeServiceLibelle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RepartionFraisEmAcqDTO repartionFraisEmAcqDTO = (RepartionFraisEmAcqDTO) o;
        if(repartionFraisEmAcqDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), repartionFraisEmAcqDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RepartionFraisEmAcqDTO{" +
            "id=" + getId() +
            ", partEm=" + getPartEm() +
            ", partAcq=" + getPartAcq() +
            "}";
    }
}
