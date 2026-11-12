package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import com.m2i.pti.domain.enumeration.TypeControl;

/**
 * A DTO for the UserControl entity.
 */
public class UserControlDTO implements Serializable {

    private Long id;

    private TypeControl action;

    private String codePays;

    private String login;

    private String password;
   private Boolean supprime;
    private String typeActeur;
    private String createdBy;
    private Instant createdDate;

    public UserControlDTO() {
    }

    public UserControlDTO(String login, String typeActeur, String createdBy, Instant createdDate) {
        this.login = login;
        this.typeActeur = typeActeur;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
    }
    public UserControlDTO(Long id,String login, String typeActeur, String action,String createdBy, Instant createdDate) {
        this.id = id;
        this.login = login;
        this.typeActeur = typeActeur;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        if(action!=null){
            this.action = TypeControl.valueOf(action);
        } else {
            this.action=TypeControl.DISABLE;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeControl getAction() {
        return action;
    }

    public void setAction(TypeControl action) {
        this.action = action;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTypeActeur() {
        return typeActeur;
    }

    public void setTypeActeur(String typeActeur) {
        this.typeActeur = typeActeur;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getSupprime() {
        return supprime;
    }

    public void setSupprime(Boolean supprime) {
        this.supprime = supprime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserControlDTO userControlDTO = (UserControlDTO) o;
        if(userControlDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), userControlDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "UserControlDTO{" +
            "id=" + getId() +
            ", action='" + getAction() + "'" +
            ", codePays='" + getCodePays() + "'" +
            ", login='" + getLogin() + "'" +
            ", password='" + getPassword() + "'" +
            ", typeActeur='" + getTypeActeur() + "'" +
            "}";
    }
}
