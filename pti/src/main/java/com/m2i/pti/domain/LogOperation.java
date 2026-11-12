package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A LogOperation.
 */
@Entity
@Table(name = "log_operation")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LogOperation extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_operation")
    private ZonedDateTime dateOperation;
    @Column(name = "description",length = 1000)
    private String description;

    @Column(name = "login")
    private String login;

    @Column(name = "result")
    private String result;

    @Column(name = "code_operation")
    private String codeOperation;
    
    @Column(name = "code_operation_ex")
    private String codeOperationEx;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getDateOperation() {
        return dateOperation;
    }

    public LogOperation dateOperation(ZonedDateTime dateOperation) {
        this.dateOperation = dateOperation;
        return this;
    }

    public void setDateOperation(ZonedDateTime dateOperation) {
        this.dateOperation = dateOperation;
    }

    public String getDescription() {
        return description;
    }

    public LogOperation description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogin() {
        return login;
    }

    public LogOperation login(String login) {
        this.login = login;
        return this;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getResult() {
        return result;
    }

    public LogOperation result(String result) {
        this.result = result;
        return this;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCodeOperation() {
        return codeOperation;
    }
 public String getCodeOperationEx() {
        return codeOperationEx;
    }

    public void setCodeOperationEx(String codeOperationEx) {
        this.codeOperationEx = codeOperationEx;
    }
    
    public LogOperation codeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
        return this;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
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
        LogOperation logOperation = (LogOperation) o;
        if (logOperation.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), logOperation.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "LogOperation{" +
            "id=" + getId() +
            ", dateOperation='" + getDateOperation() + "'" +
            ", description='" + getDescription() + "'" +
            ", login='" + getLogin() + "'" +
            ", result='" + getResult() + "'" +
            ", codeOperation='" + getCodeOperation() + "'" +
            "}";
    }

   
    
}
