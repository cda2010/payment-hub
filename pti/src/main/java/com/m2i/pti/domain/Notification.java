package com.m2i.pti.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.TypeNotification;
import com.m2i.pti.service.util.LocalDateTimeDeserializer;
import com.m2i.pti.service.util.LocalDateTimeSerializer;
import java.time.LocalDateTime;

/**
 * A Notification.
 */
@Entity
@Table(name = "notification")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Notification extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column(name = "date_heure_envoie")
    private LocalDateTime dateHeureEnvoie;

    @Column(name = "contenu")
    private String contenu;

    @Column(name = "destinataire")
    private String destinataire;

    @Column(name = "code_indicatif")
    private String codeIndicatif;

    @Column(name = "is_delivred")
    private Boolean isDelivred;

    @Column(name = "is_cousumed")
    private Boolean isConsumed;
   @Column(name = "date_consumession")
    private LocalDateTime dateConsumession;
    @Column(name = "operation_id")
    private Long operationId;
   
    @ManyToOne(fetch = FetchType.LAZY)
    private Client client;

    @ManyToOne
    private UniteOrganisation uniteOrganisation;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_notification")
    private TypeNotification typeNotification;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateHeureEnvoie() {
        return dateHeureEnvoie;
    }

    public Notification dateHeureEnvoie(LocalDateTime dateHeureEnvoie) {
        this.dateHeureEnvoie = dateHeureEnvoie;
        return this;
    }

    public void setDateHeureEnvoie(LocalDateTime dateHeureEnvoie) {
        this.dateHeureEnvoie = dateHeureEnvoie;
    }

    public String getContenu() {
        return contenu;
    }

    public Notification contenu(String contenu) {
        this.contenu = contenu;
        return this;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getDestinataire() {
        return destinataire;
    }

    public Notification destinataire(String destinataire) {
        this.destinataire = destinataire;
        return this;
    }

    public void setDestinataire(String destinataire) {
        this.destinataire = destinataire;
    }

    public Boolean isIsDelivred() {
        return isDelivred;
    }

    public Notification isDelivred(Boolean isDelivred) {
        this.isDelivred = isDelivred;
        return this;
    }

    public void setIsDelivred(Boolean isDelivred) {
        this.isDelivred = isDelivred;
    }

    public TypeNotification getTypeNotification() {
        return typeNotification;
    }

    public Notification typeNotification(TypeNotification typeNotification) {
        this.typeNotification = typeNotification;
        return this;
    }

    public Boolean getDelivred() {
        return isDelivred;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }

    public void setDelivred(Boolean delivred) {
        isDelivred = delivred;
    }

    public void setTypeNotification(TypeNotification typeNotification) {
        this.typeNotification = typeNotification;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public Boolean getIsConsumed() {
        return isConsumed;
    }

    public void setIsConsumed(Boolean isConsumed) {
        this.isConsumed = isConsumed;
    }

    public LocalDateTime getDateConsumession() {
        return dateConsumession;
    }

    public void setDateConsumession(LocalDateTime dateConsumession) {
        this.dateConsumession = dateConsumession;
    }

    public String getCodeIndicatif() {
        return codeIndicatif;
    }

    public void setCodeIndicatif(String codeIndicatif) {
        this.codeIndicatif = codeIndicatif;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Notification notification = (Notification) o;
        if (notification.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), notification.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Notification{"
                + "id=" + getId()
                + ", dateHeureEnvoie='" + getDateHeureEnvoie() + "'"
                + ", contenu='" + getContenu() + "'"
                + ", destinataire='" + getDestinataire() + "'"
                + ", isDelivred='" + isIsDelivred() + "'"
                + ", typeNotification='" + getTypeNotification() + "'"
                + "}";
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
