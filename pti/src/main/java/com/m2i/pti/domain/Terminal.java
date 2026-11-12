package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.TypeTerminal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 * A Terminal.
 */
@Entity
@Table(name = "terminal")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Terminal extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;
 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
@Column(name = "code_pays")
    private String codePays;
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "platform")
    private String platform;

    @Column(name = "model")
    private String model;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "code_terminal")
    private String codeTerminal;

    @Column(name = "code_uo")
    private String codeUo;

    @Column(name = "active_terminal")
    private Boolean activeTerminal = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_terminal")
    private TypeTerminal typeTerminal;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public Terminal uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPlatform() {
        return platform;
    }

    public Terminal platform(String platform) {
        this.platform = platform;
        return this;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getModel() {
        return model;
    }

    public Terminal model(String model) {
        this.model = model;
        return this;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public Terminal manufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getCodeTerminal() {
        return codeTerminal;
    }

    public Terminal codeTerminal(String codeTerminal) {
        this.codeTerminal = codeTerminal;
        return this;
    }

    public void setCodeTerminal(String codeTerminal) {
        this.codeTerminal = codeTerminal;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public Boolean getActiveTerminal() {
        return activeTerminal;
    }

    public void setActiveTerminal(Boolean activeTerminal) {
        this.activeTerminal = activeTerminal;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }



    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }

    public TypeTerminal getTypeTerminal() {
        return typeTerminal;
    }

    public void setTypeTerminal(TypeTerminal typeTerminal) {
        this.typeTerminal = typeTerminal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Terminal)) {
            return false;
        }
        return id != null && id.equals(((Terminal) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Terminal{"
                + "id=" + getId()
                + ", uuid='" + getUuid() + "'"
                + ", platform='" + getPlatform() + "'"
                + ", model='" + getModel() + "'"
                + ", manufacturer='" + getManufacturer() + "'"
                + ", codeTerminal='" + getCodeTerminal() + "'"
                + "}";
    }
}
