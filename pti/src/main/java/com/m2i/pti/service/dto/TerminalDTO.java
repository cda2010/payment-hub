package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.TypeTerminal;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.m2i.pti.domain.Terminal} entity.
 */
public class TerminalDTO implements Serializable {
  private String codePays;
    private Long id;

    private String uuid;

    private String platform;

    private String model;

    private String manufacturer;

    private String codeTerminal;

    private Boolean activeTerminal;

    private String codeUo;

    private TypeTerminal typeTerminal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getCodeTerminal() {
        return codeTerminal;
    }

    public void setCodeTerminal(String codeTerminal) {
        this.codeTerminal = codeTerminal;
    }

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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerminalDTO terminalDTO = (TerminalDTO) o;
        if (terminalDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), terminalDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TerminalDTO{"
                + "id=" + getId()
                + ", uuid='" + getUuid() + "'"
                + ", platform='" + getPlatform() + "'"
                + ", model='" + getModel() + "'"
                + ", manufacturer='" + getManufacturer() + "'"
                + ", codeTerminal='" + getCodeTerminal() + "'"
                + "}";
    }
}
