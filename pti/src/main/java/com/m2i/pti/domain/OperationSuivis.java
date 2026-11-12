package com.m2i.pti.domain;

import com.m2i.pti.service.dto.OperationSuivisDTO;
import com.m2i.pti.service.dto.PersonneDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A OperationSuivis.
 */
@NamedNativeQueries({
        @NamedNativeQuery(name = "OperationSuivis.findByClientId",
                query = "select id, client_id clientId, operation_id operationId "
                        + "from  operation_suivis   "
                        + " where  client_id=:clientId",
                resultSetMapping = "Mapping.OperationSuivisDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
        ,
})
@SqlResultSetMappings({
        @SqlResultSetMapping(name = "Mapping.OperationSuivisDTO",
                classes = @ConstructorResult(targetClass = OperationSuivisDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class)
                                ,
                                @ColumnResult(name = "clientId", type = Long.class)
                                ,
                                @ColumnResult(name = "operationId", type = Long.class),
                        })),

})

@Entity
@Table(name = "operation_suivis")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class OperationSuivis extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Client client;

    @ManyToOne
    private Operation operation;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public OperationSuivis client(Client client) {
        this.client = client;
        return this;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Operation getOperation() {
        return operation;
    }

    public OperationSuivis operation(Operation operation) {
        this.operation = operation;
        return this;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
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
        OperationSuivis operationSuivis = (OperationSuivis) o;
        if (operationSuivis.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), operationSuivis.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "OperationSuivis{" +
            "id=" + getId() +
            "}";
    }
}
