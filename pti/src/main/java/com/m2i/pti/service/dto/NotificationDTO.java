package com.m2i.pti.service.dto;


import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Objects;
import com.m2i.pti.domain.enumeration.TypeNotification;

/**
 * A DTO for the Notification entity.
 */
public class NotificationDTO implements Serializable {

    private Long id;
    private Long clientId;
    private Long uniteOrganisationId;
    private LocalDateTime dateHeureEnvoie;

    private String contenu;

    private String destinataire;

    private Boolean isDelivred;

    private TypeNotification typeNotification;

    private String codeIndicatif;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateHeureEnvoie() {
        return dateHeureEnvoie;
    }

    public void setDateHeureEnvoie(LocalDateTime dateHeureEnvoie) {
        this.dateHeureEnvoie = dateHeureEnvoie;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(String destinataire) {
        this.destinataire = destinataire;
    }

    public Boolean isIsDelivred() {
        return isDelivred;
    }

    public void setIsDelivred(Boolean isDelivred) {
        this.isDelivred = isDelivred;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Boolean getDelivred() {
        return isDelivred;
    }

    public void setDelivred(Boolean delivred) {
        isDelivred = delivred;
    }

    public Long getUniteOrganisationId() {
        return uniteOrganisationId;
    }

    public String getCodeIndicatif() {
        return codeIndicatif;
    }

    public void setCodeIndicatif(String codeIndicatif) {
        this.codeIndicatif = codeIndicatif;
    }

    public void setUniteOrganisationId(Long uniteOrganisationId) {
        this.uniteOrganisationId = uniteOrganisationId;
    }

    public TypeNotification getTypeNotification() {
        return typeNotification;
    }

    public void setTypeNotification(TypeNotification typeNotification) {
        this.typeNotification = typeNotification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NotificationDTO notificationDTO = (NotificationDTO) o;
        if(notificationDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), notificationDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "NotificationDTO{" +
            "id=" + getId() +
            ", dateHeureEnvoie='" + getDateHeureEnvoie() + "'" +
            ", contenu='" + getContenu() + "'" +
            ", destinataire='" + getDestinataire() + "'" +
            ", isDelivred='" + isIsDelivred() + "'" +
            ", typeNotification='" + getTypeNotification() + "'" +
            "}";
    }
}
