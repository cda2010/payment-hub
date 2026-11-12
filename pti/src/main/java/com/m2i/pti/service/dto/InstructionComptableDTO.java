package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.Objects;
import com.m2i.pti.domain.enumeration.InstructionEcriture;
import com.m2i.pti.domain.enumeration.InstructionUo;
import com.m2i.pti.domain.enumeration.MontantsAComptabiliser;

/**
 * A DTO for the InstructionComptable entity.
 */
public class InstructionComptableDTO implements Serializable {

    private Long id;

    private Integer numeroOrdre;

    private InstructionEcriture instruction;

    private InstructionUo instructionUo;
    private String etiquetteComptable;

    private MontantsAComptabiliser montant;

    private Long typeServiceId;

    private String typeServiceLibelle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroOrdre() {
        return numeroOrdre;
    }

    public void setNumeroOrdre(Integer numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }

    public InstructionEcriture getInstruction() {
        return instruction;
    }

    public void setInstruction(InstructionEcriture instruction) {
        this.instruction = instruction;
    }

    public String getEtiquetteComptable() {
        return etiquetteComptable;
    }

    public void setEtiquetteComptable(String etiquetteComptable) {
        this.etiquetteComptable = etiquetteComptable;
    }

    public MontantsAComptabiliser getMontant() {
        return montant;
    }

    public void setMontant(MontantsAComptabiliser montant) {
        this.montant = montant;
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

    public InstructionUo getInstructionUo() {
        return instructionUo;
    }

    public void setInstructionUo(InstructionUo instructionUo) {
        this.instructionUo = instructionUo;
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InstructionComptableDTO instructionComptableDTO = (InstructionComptableDTO) o;
        if(instructionComptableDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), instructionComptableDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "InstructionComptableDTO{" +
            "id=" + getId() +
            ", numeroOrdre=" + getNumeroOrdre() +
            ", instruction='" + getInstruction() + "'" +
            ", etiquetteComptable='" + getEtiquetteComptable() + "'" +
            ", montant='" + getMontant() + "'" +
            "}";
    }
}
