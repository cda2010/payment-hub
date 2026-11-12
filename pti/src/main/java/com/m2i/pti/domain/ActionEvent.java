/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.ActionEnum;
import com.m2i.pti.domain.enumeration.FirstCategorieCompte;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import com.m2i.pti.service.dto.ActionEventDTO;
import com.m2i.pti.service.dto.PersonneDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@NamedNativeQueries({
        @NamedNativeQuery(name = "ActionEvent.findAllByActionAndDateAction",
                query = "select entity_id as entityId,login ,objet,action as action,date_action as dateAction  from action_event a where a.action=:actionE and a.date_action between CAST(CAST(:dateDebut as text) as timestamp) and CAST(CAST(:dateFin as text) as timestamp)",
                resultSetMapping = "Mapping.ActionEventDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

})

@SqlResultSetMappings({
        @SqlResultSetMapping(name = "Mapping.ActionEventDTO",
                classes = @ConstructorResult(targetClass = ActionEventDTO.class,
                        columns = {
                                @ColumnResult(name = "entityId", type = String.class)
                                ,
                                @ColumnResult(name = "login", type = String.class)
                                ,
                                @ColumnResult(name = "objet", type = String.class)
                                ,
                                @ColumnResult(name = "action", type = String.class)
                                ,
                                @ColumnResult(name = "dateAction", type = LocalDateTime.class)


                        })),
})


/**
 *
 * @author cda
 */
@Entity
@Table(name = "action_event")

public class ActionEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;
    @Column(name = "entity_id")
    private String entityId;
    @Column(name = "login")
    private String login;
    @Column(name = "objet")
    private String objet;
    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private ActionEnum action;
     @Column(name = "date_action")
    private LocalDateTime dateAction;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public ActionEnum getAction() {
        return action;
    }

    public void setAction(ActionEnum action) {
        this.action = action;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public LocalDateTime getDateAction() {
        return dateAction;
    }

    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }

}
