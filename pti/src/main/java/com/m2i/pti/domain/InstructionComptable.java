package com.m2i.pti.domain;

import com.m2i.pti.config.audit.EntityAuditEventListener;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.InstructionEcriture;
import com.m2i.pti.domain.enumeration.InstructionUo;

import com.m2i.pti.domain.enumeration.MontantsAComptabiliser;

/**
 * A InstructionComptable.
 */
@Entity
@Table(name = "instruction_comptable")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(value = {EntityAuditEventListener.class})
public class InstructionComptable extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_ordre")
    private Integer numeroOrdre;

    @Enumerated(EnumType.STRING)
    @Column(name = "instruction")
    private InstructionEcriture instruction;

    @Column(name = "etiquette_comptable")
    private String etiquetteComptable;

    @Enumerated(EnumType.STRING)
    @Column(name = "montant")
    private MontantsAComptabiliser montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "instructionUo")
    private InstructionUo instructionUo;
     
    @ManyToOne
    private TypeService typeService;
    
    

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroOrdre() {
        return numeroOrdre;
    }

    public InstructionComptable numeroOrdre(Integer numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
        return this;
    }

    public void setNumeroOrdre(Integer numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }

    public InstructionEcriture getInstruction() {
        return instruction;
    }

    public InstructionComptable instruction(InstructionEcriture instruction) {
        this.instruction = instruction;
        return this;
    }

    public void setInstruction(InstructionEcriture instruction) {
        this.instruction = instruction;
    }

    public String getEtiquetteComptable() {
        return etiquetteComptable;
    }

    public InstructionComptable etiquetteComptable(String etiquetteComptable) {
        this.etiquetteComptable = etiquetteComptable;
        return this;
    }

    public void setEtiquetteComptable(String etiquetteComptable) {
        this.etiquetteComptable = etiquetteComptable;
    }

    public MontantsAComptabiliser getMontant() {
        return montant;
    }

    public InstructionComptable montant(MontantsAComptabiliser montant) {
        this.montant = montant;
        return this;
    }

    public void setMontant(MontantsAComptabiliser montant) {
        this.montant = montant;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public InstructionComptable typeService(TypeService typeService) {
        this.typeService = typeService;
        return this;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public InstructionUo getInstructionUo() {
        return instructionUo;
    }

    public void setInstructionUo(InstructionUo instructionUo) {
        this.instructionUo = instructionUo;
    }
    
    
    public InstructionComptable instructionUo(InstructionUo instructionUo) {
        this.instructionUo = instructionUo;
        return this;
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
        InstructionComptable instructionComptable = (InstructionComptable) o;
        if (instructionComptable.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), instructionComptable.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "InstructionComptable{" +
            "id=" + getId() +
            ", numeroOrdre=" + getNumeroOrdre() +
            ", instruction='" + getInstruction() + "'" +
            ", etiquetteComptable='" + getEtiquetteComptable() + "'" +
            ", montant='" + getMontant() + "'" +
            "}";
    }
}
