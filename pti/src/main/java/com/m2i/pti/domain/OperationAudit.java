package com.m2i.pti.domain;

import com.m2i.pti.service.dto.OperationAuditDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.StatutOperation;

/**
 * A OperationAudit.
 */
@SqlResultSetMapping(name = "Mapping.OperationAuditDTO",
        classes = @ConstructorResult(targetClass = OperationAuditDTO.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "jhi_date", type = ZonedDateTime.class),
                        @ColumnResult(name = "statut_precedent", type = String.class),
                        @ColumnResult(name = "statut_apres", type = String.class),
                        @ColumnResult(name = "code_operation", type = String.class)
                }))

@NamedNativeQueries({
        @NamedNativeQuery(name = "OperationAudit.findAllAuditOperation",
                query = "select op.id,op.jhi_date,op.statut_precedent,op.statut_apres,op.operation_id,ope.code_operation from operation_audit op,operation ope where ((op.statut_precedent=CAST(:statutPrecedent AS TEXT) or :statutPrecedent is null ) and " +
                        "(op.statut_apres=CAST(:statutApres AS TEXT) or :statutApres is null ) and op.operation_id = ope.id) order by op.id desc",
                resultSetMapping = "Mapping.OperationAuditDTO",
                hints = {
                        @QueryHint(name = "org.hibernate.cacheable", value = "false")}),
})
@Entity
@Table(name = "operation_audit")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class OperationAudit extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jhi_date")
    private ZonedDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_precedent")
    private StatutOperation statutPrecedent;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_apres")
    private StatutOperation statutApres;

    @ManyToOne
    private Operation operation;

    @ManyToOne
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public OperationAudit date(ZonedDateTime date) {
        this.date = date;
        return this;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public StatutOperation getStatutPrecedent() {
        return statutPrecedent;
    }

    public OperationAudit statutPrecedent(StatutOperation statutPrecedent) {
        this.statutPrecedent = statutPrecedent;
        return this;
    }

    public void setStatutPrecedent(StatutOperation statutPrecedent) {
        this.statutPrecedent = statutPrecedent;
    }

    public StatutOperation getStatutApres() {
        return statutApres;
    }

    public OperationAudit statutApres(StatutOperation statutApres) {
        this.statutApres = statutApres;
        return this;
    }

    public void setStatutApres(StatutOperation statutApres) {
        this.statutApres = statutApres;
    }

    public Operation getOperation() {
        return operation;
    }

    public OperationAudit operation(Operation operation) {
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
        OperationAudit operationAudit = (OperationAudit) o;
        if (operationAudit.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), operationAudit.getId());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OperationAudit user(User user) {
        this.user = user;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "OperationAudit{" +
            "id=" + getId() +
            ", date='" + getDate() + "'" +
            ", statutPrecedent='" + getStatutPrecedent() + "'" +
            ", statutApres='" + getStatutApres() + "'" +
            "}";
    }
}
