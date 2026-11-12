package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.Destination;

import com.m2i.pti.domain.enumeration.Langue;
import com.m2i.pti.domain.enumeration.TypeServiceLibelle;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A TemplateMessage.
 */
@Entity
@Table(name = "template_message")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable(value = true)
public class TemplateMessage extends AbstractAuditingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etiquette")
    private String etiquette;

    @Column(name = "contenu")
    private String contenu;


    @Enumerated(EnumType.STRING)
    @Column(name = "langue")
    private Langue langue;


    @Enumerated(EnumType.STRING)
    @Column(name = "destination")
    private Destination destination;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_service_libelle")
    private TypeServiceLibelle typeServiceLibelle;


    @ManyToOne
    private UniteOrganisation fse ;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEtiquette() {
        return etiquette;
    }

    public TemplateMessage etiquette(String etiquette) {
        this.etiquette = etiquette;
        return this;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    public String getContenu() {
        return contenu;
    }

    public TemplateMessage contenu(String contenu) {
        this.contenu = contenu;
        return this;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Langue getLangue() {
        return langue;
    }

    public TemplateMessage langue(Langue langue) {
        this.langue = langue;
        return this;
    }


    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public TypeServiceLibelle getTypeServiceLibelle() {
        return typeServiceLibelle;
    }

    public void setTypeServiceLibelle(TypeServiceLibelle typeServiceLibelle) {
        this.typeServiceLibelle = typeServiceLibelle;
    }


    public UniteOrganisation getFse() {
        return fse;
    }

    public void setFse(UniteOrganisation fse) {
        this.fse = fse;
    }

    public void setLangue(Langue langue) {
        this.langue = langue;
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
        TemplateMessage templateMessage = (TemplateMessage) o;
        if (templateMessage.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), templateMessage.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TemplateMessage{" +
            "id=" + getId() +
            ", etiquette='" + getEtiquette() + "'" +
            ", contenu='" + getContenu() + "'" +
            ", langue='" + getLangue() + "'" +
            "}";
    }
}
