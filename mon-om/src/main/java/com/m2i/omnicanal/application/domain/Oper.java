/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.domain;

import com.m2i.omnicanal.application.service.dto.OperationDTO;
import com.m2i.omnicanal.model.Operation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.QueryHint;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;

/**
 *
 * @author cda
 */
@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.OperationDTO",
            classes = @ConstructorResult(targetClass = OperationDTO.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                        @ColumnResult(name = "reference", type = String.class)
                        ,
                        @ColumnResult(name = "fseCodeSource", type = String.class)
                        ,
                        @ColumnResult(name = "fseCodeDest", type = String.class)
                        ,
                        @ColumnResult(name = "libelleTypeService", type = String.class),
                        @ColumnResult(name = "terminal", type = String.class)
                        ,@ColumnResult(name = "textTransaction", type = String.class)
                        , @ColumnResult(name = "dateOperation", type = ZonedDateTime.class),
                        
                        @ColumnResult(name = "ack", type = Integer.class)
                       

                    }))

})
@NamedNativeQueries({
    @NamedNativeQuery(name = "Operation.findOperation",
            query = "select op.id as id, reference , fse_code_source as fseCodeSource, fse_code_dest as fseCodeDest,  libelle_type_service as libelleTypeService,terminal, text_transaction textTransaction , date_operation dateOperation, ack from operation op, type_service t where  "
            + " t.id= op.type_service_id and "
            + " ((op.fse_code_source = coalesce(:pfseSrc,op.fse_code_source)) AND "
            + "        (op.date_operation >= coalesce(:pdebut,op.date_operation)) AND (op.date_operation <= coalesce(:pfin,op.date_operation)) AND "
            + "         (op.fse_code_dest = coalesce(:pfseDest,op.fse_code_dest)) AND (op.type_service_id = coalesce(:ptypeServiceId,op.type_service_id)) AND "
            + "         (op.reference = coalesce(:pnumero,op.reference))) order by  op.date_operation desc ",
            resultSetMapping = "Mapping.OperationDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
     @NamedNativeQuery(name = "Operation.findTop10Operation",
            query = "select op.id as id, reference , fse_code_source as fseCodeSource, fse_code_dest as fseCodeDest,  libelle_type_service as libelleTypeService,terminal, text_transaction textTransaction , date_operation dateOperation, ack from operation op, type_service t where  "
            + " t.id= op.type_service_id  "
            + "order by  op.date_operation desc limit 10 ",
            resultSetMapping = "Mapping.OperationDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})
    ,
    

@NamedNativeQuery(name = "Operation.findAll",
            query = "select op.id, reference , fse_code_source as fseCodeSource, fse_code_dest as fseCodeDest,  libelle_type_service as libelleTypeService,terminal, text_transaction textTransaction, date_operation dateOperation, ack from operation op, type_service t where  "
            + " t.id= op.type_service_id order by  op.date_operation desc ",
             resultSetMapping = "Mapping.OperationDTO", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")})})
@Entity
public class Oper extends Operation implements Serializable {

    private static final long serialVersionUID = 1L;
    private String fseSourceNom;
    private String fseDestNom;

    public String getFseSourceNom() {
        return fseSourceNom;
    }

    public void setFseSourceNom(String fseSourceNom) {
        this.fseSourceNom = fseSourceNom;
    }

    public String getFseDestNom() {
        return fseDestNom;
    }

    public void setFseDestNom(String fseDestNom) {
        this.fseDestNom = fseDestNom;
    }
}
