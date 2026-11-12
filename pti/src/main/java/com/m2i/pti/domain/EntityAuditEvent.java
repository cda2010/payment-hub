package com.m2i.pti.domain;

import com.m2i.pti.service.dto.EntityAuditEventDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@SqlResultSetMapping(name = "Mapping.EntityAuditEventDTO",
        classes = @ConstructorResult(targetClass = EntityAuditEventDTO.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "entity_id", type = Long.class),
                        @ColumnResult(name = "entity_type", type = String.class),
                        @ColumnResult(name = "action", type = String.class),
                        @ColumnResult(name = "entity_value", type = String.class),
                        @ColumnResult(name = "commit_version", type = Integer.class),
                        @ColumnResult(name = "modified_by", type = String.class),
                        @ColumnResult(name = "adresse", type = String.class),
                        @ColumnResult(name = "modified_date", type = Instant.class)
                }))

@NamedNativeQueries({
        @NamedNativeQuery(name = "EntityAuditEvent.findAllAuditEvent",
                query = "SELECT jrae.id,jrae.entity_id,jrae.entity_type,jrae.action,jrae.entity_value,jrae.commit_version,jrae.modified_by,jrae.adresse,jrae.modified_date FROM jhi_entity_audit_event jrae WHERE (" +
                        "(:action is null or :action='' or UPPER(jrae.action) " +
                        "like UPPER('%'||CAST(:action AS TEXT)||'%')) and (:adresse is null or :adresse='' or UPPER(jrae.adresse) " +
                        "like UPPER('%'||CAST(:adresse AS TEXT)||'%')) and (:modifiedBy is null or :modifiedBy='' or UPPER(jrae.modified_by) " +
                        "like UPPER('%'||CAST(:modifiedBy AS TEXT)||'%'))) order by jrae.id desc",
                resultSetMapping = "Mapping.EntityAuditEventDTO",
                hints = {
                        @QueryHint(name = "org.hibernate.cacheable", value = "false")}),
})
@Entity
@Table(name = "jhi_entity_audit_event")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class EntityAuditEvent extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @NotNull
    @Size(max = 255)
    @Column(name = "entity_type", length = 255, nullable = false)
    private String entityType;

    @NotNull
    @Size(max = 255)
    @Column(name = "action", length = 255, nullable = false)
    private String action;

    
    @Column(name = "entity_old_value")
    private String entityOldValue;

    @Column(name = "entity_value")
    private String entityValue;

    
    @Column(name = "entity_value_json")
    private String entityValuejson;
    
    
    @Column(name = "commit_version")
    private Integer commitVersion;

    @Size(max = 255)
    @Column(name = "modified_by", length = 255)
    private String modifiedBy;

    @NotNull
    @Column(name = "modified_date", nullable = false)
    private Instant modifiedDate;

    @Column(name = "adresse")
    private String adresse;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityValue() {
        return entityValue;
    }

    public void setEntityValue(String entityValue) {
        this.entityValue = entityValue;
    }

    public Integer getCommitVersion() {
        return commitVersion;
    }

    public void setCommitVersion(Integer commitVersion) {
        this.commitVersion = commitVersion;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Instant getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Instant modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getEntityOldValue() {
        return entityOldValue;
    }

    public void setEntityOldValue(String entityOldValue) {
        this.entityOldValue = entityOldValue;
    }

    public String getEntityValuejson() {
        return entityValuejson;
    }

    public void setEntityValuejson(String entityValuejson) {
        this.entityValuejson = entityValuejson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityAuditEvent entityAuditEvent = (EntityAuditEvent) o;
        return Objects.equals(id, entityAuditEvent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "[id=" + id
                + ", entityId='" + entityId + "'"
                + ", entityType='" + entityType + "'"
                + ", action='" + action + "'"
                + ", OldValue='" + entityOldValue + "'"
                + ", entityValue='" + entityValue + "'"
                + ", commitVersion='" + commitVersion + "'"
                + ", modifiedBy='" + modifiedBy + "'"
                + ", modifiedDate='" + modifiedDate + "'"
                + ']';
    }

}
