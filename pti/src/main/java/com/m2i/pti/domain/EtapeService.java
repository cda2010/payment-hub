/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.ActionService;
import com.m2i.pti.service.dto.EtapeServiceDTO;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.*;


/**
 *
 * @author cda
 */
@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.EtapeServiceDTO",
            classes = @ConstructorResult(targetClass = EtapeServiceDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "type_service_id", type = Long.class),
                        @ColumnResult(name = "action_service", type = String.class),
                        @ColumnResult(name = "code", type = String.class),
                       
                    }))
}) 
@NamedNativeQueries({
    @NamedNativeQuery(name = "EtapeService.findAllEtapeService",
            query = "select eta.id, eta.action_service, eta.type_service_id,ts.code FROM etape_service eta inner join type_service ts on ts.id = eta.type_service_id WHERE (" 
            +"(:actionService is null or  eta.action_service=CAST(:actionService AS TEXT))" 
            +") order by eta.id desc",
          //  resultSetMapping = "Mapping.EtapeServiceDTO", 
            hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),  
            })
@Entity
@Table(name = "etapeService")
public class EtapeService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


 //   @ElementCollection(targetClass = ActionService.class)
    @Enumerated(EnumType.STRING)
    @Column(name = "action_service")
    private ActionService actionService;

    @ManyToOne
    private TypeService typeService;
 
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }                                                                                                                                                              

    

    public ActionService getActionService() {
        return actionService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }
    
   /* public List<String> getActionServicesAsString(){
        return this.actionService.stream().map(Enum::toString).collect(Collectors.toList());
    }*/
    
}
