/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.domain;

import com.m2i.omnicanal.application.service.dto.DataChartResponse;
import com.m2i.omnicanal.application.service.dto.OperationDTO;
import java.io.Serializable;
import java.time.ZonedDateTime;
import jakarta.persistence.Cacheable;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.QueryHint;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 *
 * @author cda
 */
@SqlResultSetMappings(value = {
    @SqlResultSetMapping(name = "Mapping.DataChartResponse",
            classes = @ConstructorResult(targetClass = DataChartResponse.class,
                    columns = {
                          @ColumnResult(name = "label", type = String.class),
                        @ColumnResult(name = "valeur", type = Long.class)
                        
                      

                    }))

})
@NamedNativeQueries({
    @NamedNativeQuery(name = "HistAppel.findNombrePerFonction",
            query = "select  nom_fonction as label, count(*) as valeur from historique_appel group by nom_fonction",
            resultSetMapping = "Mapping.DataChartResponse", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),
    @NamedNativeQuery(name = "HistAppel.findNombrePerCodeError",
            query = "select  code_reponse as label, count(*) as valeur from historique_appel group by code_reponse",
            resultSetMapping = "Mapping.DataChartResponse", hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "false")}),

})
@Entity
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HistAppel extends HistoriqueAppel implements Serializable {

    private static final long serialVersionUID = 1L;

}
