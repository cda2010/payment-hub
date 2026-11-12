/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.service.dto.ActiviteClientDTO;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.QueryHint;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.ActiviteClientDTO",
            classes = @ConstructorResult(targetClass = ActiviteClientDTO.class,
                    columns = {
                        @ColumnResult(name = "description", type = String.class)
                        ,
                        @ColumnResult(name = "reference", type = String.class)
                        ,
                          
                        @ColumnResult(name = "dateOperation", type = ZonedDateTime.class)
                        ,
                              @ColumnResult(name = "statut", type = String.class)
                        ,
                              @ColumnResult(name = "montant", type = BigDecimal.class)

                    }))
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "activiteClientDTO.findActivite",
            query = "select   libelle as description  , reference,date_operation as dateOperation, statut, montant from activite_client ac_cl, type_service ts, personne pers "
            + " where ts.id= ac_cl.type_service_id "
            + " and pers.id= ac_cl.personne_id"
            + " and  ( ( pers.telephone=:telephone and pers.code_telephone=:codeTelephone))",
            resultSetMapping = "Mapping.ActiviteClientDTO",
            hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "true")}),})
/**
 *
 * @author cda
 */
@Entity
@Table(name = "activite_client")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ActiviteClient extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Personne personne;
    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutOperation statut;
    @ManyToOne
    private TypeService typeService;
    @Column(name = "montant")
    private BigDecimal montant;

    @Column(name = "reference")
    private String reference;

    @Column(name = "codeFse")
    private String codeFse;

    @Column(name = "date_operation")
    private ZonedDateTime dateOperation;

    public Personne getPersonne() {
        return personne;
    }

    public void setPersonne(Personne personne) {
        this.personne = personne;
    }

    public StatutOperation getStatut() {
        return statut;
    }

    public void setStatut(StatutOperation statut) {
        this.statut = statut;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public ZonedDateTime getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(ZonedDateTime dateOperation) {
        this.dateOperation = dateOperation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

}
