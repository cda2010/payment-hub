package com.m2i.pti.domain;

import com.m2i.pti.service.dto.ActionEventDTO;
import com.m2i.pti.service.dto.UserControlDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.TypeControl;

@NamedNativeQueries({
    @NamedNativeQuery(name = "UserControl.findAllByAction",
            query = "select login,type_acteur as typeActeur,created_by as createdBy,created_date as createdDate from user_control uc where uc.action=:typeControl and supprime=true and uc.created_date between CAST(CAST(:dateDebut as text) as timestamp) and CAST(CAST(:dateFin as text) as timestamp)",
            resultSetMapping = "Mapping.UserControlDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),

     @NamedNativeQuery(name = "UserControl.findAllByPersonneDate",
                query = "select uc.id,login,type_acteur as typeActeur,action,created_by as createdBy,created_date as createdDate from user_control uc where (:telephone is null or uc.login=:telephone)  and uc.created_date between CAST(CAST(:debut as text) as timestamp) and CAST(CAST(:fin as text) as timestamp) order by uc.id desc limit :taille offset :page",
                resultSetMapping = "Mapping.UserControlDTON", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),
@NamedNativeQuery(name = "UserControl.findUserByAction",
                query = "select uc.id,login,type_acteur as typeActeur,action,created_by as createdBy,created_date as createdDate from user_control uc where uc.action=:action and supprime=false and uc.login=:telephone order by uc.id desc",
                resultSetMapping = "Mapping.UserControlDTON", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),
    @NamedNativeQuery(name = "UserControl.findAllByPersonne",
                query = "select uc.id,login,type_acteur as typeActeur,action,created_by as createdBy,created_date as createdDate from user_control uc where (:telephone is null or uc.login=:telephone)  order by uc.id desc limit :taille offset :page",
                resultSetMapping = "Mapping.UserControlDTON", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),
})

@SqlResultSetMappings({
    @SqlResultSetMapping(name = "Mapping.UserControlDTO",
            classes = @ConstructorResult(targetClass = UserControlDTO.class,
                    columns = {
                        @ColumnResult(name = "login", type = String.class)
                        ,
                                @ColumnResult(name = "typeActeur", type = String.class)
                        ,
                                @ColumnResult(name = "createdBy", type = String.class)
                        ,
                                @ColumnResult(name = "createdDate", type = Instant.class)

                    })),

        @SqlResultSetMapping(name = "Mapping.UserControlDTON",
                classes = @ConstructorResult(targetClass = UserControlDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class)
                                ,
                                @ColumnResult(name = "login", type = String.class)
                                ,
                                @ColumnResult(name = "typeActeur", type = String.class)
                                ,
                                @ColumnResult(name = "action", type = String.class)
                                ,
                                @ColumnResult(name = "createdBy", type = String.class)
                                ,
                                @ColumnResult(name = "createdDate", type = Instant.class)

                        })),
})
/**
 * A UserControl.
 */
@Entity
@Table(name = "user_control")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserControl extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private TypeControl action;

    @Column(name = "code_pays")
    private String codePays;

    @Column(name = "login")
    private String login;

    @Column(name = "jhi_password")
    private String password;

    @Column(name = "type_acteur")
    private String typeActeur;

    @Column(name = "supprime")
    private Boolean supprime = false;

    @Column(name = "canal")
    private String canal;

    @Column(name = "data", columnDefinition = "TEXT")

    private String data;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeControl getAction() {
        return action;
    }

    public UserControl action(TypeControl action) {
        this.action = action;
        return this;
    }

    public void setAction(TypeControl action) {
        this.action = action;
    }

    public String getCodePays() {
        return codePays;
    }

    public UserControl codePays(String codePays) {
        this.codePays = codePays;
        return this;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public String getLogin() {
        return login;
    }

    public UserControl login(String login) {
        this.login = login;
        return this;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public UserControl password(String password) {
        this.password = password;
        return this;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTypeActeur() {
        return typeActeur;
    }

    public UserControl typeActeur(String typeActeur) {
        this.typeActeur = typeActeur;
        return this;
    }

    public Boolean getSupprime() {
        return supprime;
    }

    public void setSupprime(Boolean supprime) {
        this.supprime = supprime;
    }

    public void setTypeActeur(String typeActeur) {
        this.typeActeur = typeActeur;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserControl userControl = (UserControl) o;
        if (userControl.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), userControl.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "UserControl{"
                + "id=" + getId()
                + ", action='" + getAction() + "'"
                + ", codePays='" + getCodePays() + "'"
                + ", login='" + getLogin() + "'"
                + ", password='" + getPassword() + "'"
                + ", typeActeur='" + getTypeActeur() + "'"
                + "}";
    }
}
