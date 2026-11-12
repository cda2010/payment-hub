package com.m2i.pti.service.dto;

import java.io.Serializable;
import java.time.Instant;

public class EntityAuditEventDTO implements Serializable {

    private Long id;

    private Long entityId;

    private String entityType;

    private String action;

    private String entityOldValue;

    private String entityValue;

    private String entityValuejson;

    private Integer commitVersion;

    private String modifiedBy;

    private Instant modifiedDate;

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

    public String getEntityOldValue() {
        return entityOldValue;
    }

    public void setEntityOldValue(String entityOldValue) {
        this.entityOldValue = entityOldValue;
    }

    public String getEntityValue() {
        return entityValue;
    }

    public void setEntityValue(String entityValue) {
        this.entityValue = entityValue;
    }

    public String getEntityValuejson() {
        return entityValuejson;
    }

    public void setEntityValuejson(String entityValuejson) {
        this.entityValuejson = entityValuejson;
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

    public EntityAuditEventDTO(Long id, Long entityId, String entityType, String action, String entityValue,
                               Integer commitVersion, String modifiedBy, String adresse, Instant modifiedDate) {
        this.id = id;
        this.entityId = entityId;
        this.entityType = entityType;
        this.action = action;
        this.entityValue = entityValue;
        this.commitVersion = commitVersion;
        this.modifiedBy = modifiedBy;
        this.adresse = adresse;
        this.modifiedDate = modifiedDate;
    }

    public EntityAuditEventDTO() {
    }
}
