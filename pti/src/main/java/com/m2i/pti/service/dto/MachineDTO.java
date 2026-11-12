package com.m2i.pti.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the Machine entity.
 */
public class MachineDTO implements Serializable {

    private Long id;

    private String uuid;

    private String hostName;

    private String currentUser;

    private String macAddress;

    private String hostId;

    private String computerManufacturer;

    private String computerSerialNumber;

    private String computerModel;

    private String firwareManufacturer;

    private String firwareName;

    private String firwareDescription;

    private String processorIdentifier;

    private String processorId;

    private String networkInterfaces;

    private String networkParameters;

    private String telephone;

    private Boolean enabled;

    private String libelleUniteOrganisation;

    private Long organisationId;

    private byte[] detailsMachine;

    private String detailsMachineContentType;

    private String cle;

    private String emailCertificat;

    private String emailCodeAcces;


    private String tempPass;

    private String adresseIp;

    public MachineDTO() {
    }

    public MachineDTO(Long id, String uuid, String hostName, String currentUser, String macAddress, String hostId, String computerManufacturer, String computerSerialNumber, String computerModel, String firwareManufacturer, String firwareName, String firwareDescription, String processorIdentifier, String processorId, String networkInterfaces, String networkParameters, Boolean enabled, String emailCertificat, String emailCodeAcces, String tempPass, String adresseIp, byte[] detailsMachine, String detailsMachineContentType, Long organisationId, String libelleUniteOrganisation) {
        this.id = id;
        this.uuid = uuid;
        this.hostName = hostName;
        this.currentUser = currentUser;
        this.macAddress = macAddress;
        this.hostId = hostId;
        this.computerManufacturer = computerManufacturer;
        this.computerSerialNumber = computerSerialNumber;
        this.computerModel = computerModel;
        this.firwareManufacturer = firwareManufacturer;
        this.firwareName = firwareName;
        this.firwareDescription = firwareDescription;
        this.processorIdentifier = processorIdentifier;
        this.processorId = processorId;
        this.networkInterfaces = networkInterfaces;
        this.networkParameters = networkParameters;
        this.enabled = enabled;
        this.emailCertificat = emailCertificat;
        this.emailCodeAcces = emailCodeAcces;
        this.tempPass = tempPass;
        this.adresseIp = adresseIp;
        this.detailsMachine = detailsMachine;
        this.detailsMachineContentType = detailsMachineContentType;
        this.organisationId = organisationId;
        this.libelleUniteOrganisation = libelleUniteOrganisation;
    }


    public String getTempPass() {
        return tempPass;
    }

    public void setTempPass(String tempPass) {
        this.tempPass = tempPass;
    }

    public String getAdresseIp() {
        return adresseIp;
    }

    public void setAdresseIp(String adresseIp) {
        this.adresseIp = adresseIp;
    }

    public String getEmailCertificat() {
        return emailCertificat;
    }

    public void setEmailCertificat(String emailCertificat) {
        this.emailCertificat = emailCertificat;
    }

    public String getEmailCodeAcces() {
        return emailCodeAcces;
    }

    public void setEmailCodeAcces(String emailCodeAcces) {
        this.emailCodeAcces = emailCodeAcces;
    }

    public String getCle() {
        return cle;
    }

    public void setCle(String cle) {
        this.cle = cle;
    }

    public String getLibelleUniteOrganisation() {
        return libelleUniteOrganisation;
    }

    public void setLibelleUniteOrganisation(String libelleUniteOrganisation) {
        this.libelleUniteOrganisation = libelleUniteOrganisation;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getComputerManufacturer() {
        return computerManufacturer;
    }

    public void setComputerManufacturer(String computerManufacturer) {
        this.computerManufacturer = computerManufacturer;
    }

    public String getComputerSerialNumber() {
        return computerSerialNumber;
    }

    public void setComputerSerialNumber(String computerSerialNumber) {
        this.computerSerialNumber = computerSerialNumber;
    }

    public String getComputerModel() {
        return computerModel;
    }

    public void setComputerModel(String computerModel) {
        this.computerModel = computerModel;
    }

    public String getFirwareManufacturer() {
        return firwareManufacturer;
    }

    public void setFirwareManufacturer(String firwareManufacturer) {
        this.firwareManufacturer = firwareManufacturer;
    }

    public String getFirwareName() {
        return firwareName;
    }

    public void setFirwareName(String firwareName) {
        this.firwareName = firwareName;
    }

    public String getFirwareDescription() {
        return firwareDescription;
    }

    public void setFirwareDescription(String firwareDescription) {
        this.firwareDescription = firwareDescription;
    }

    public String getProcessorIdentifier() {
        return processorIdentifier;
    }

    public void setProcessorIdentifier(String processorIdentifier) {
        this.processorIdentifier = processorIdentifier;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public String getNetworkInterfaces() {
        return networkInterfaces;
    }

    public void setNetworkInterfaces(String networkInterfaces) {
        this.networkInterfaces = networkInterfaces;
    }

    public String getNetworkParameters() {
        return networkParameters;
    }

    public void setNetworkParameters(String networkParameters) {
        this.networkParameters = networkParameters;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public byte[] getDetailsMachine() {
        return detailsMachine;
    }

    public void setDetailsMachine(byte[] detailsMachine) {
        this.detailsMachine = detailsMachine;
    }

    public String getDetailsMachineContentType() {
        return detailsMachineContentType;
    }

    public void setDetailsMachineContentType(String detailsMachineContentType) {
        this.detailsMachineContentType = detailsMachineContentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MachineDTO machineDTO = (MachineDTO) o;
        if (machineDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), machineDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "MachineDTO{" +
            "id=" + getId() +
            ", uuid='" + getUuid() + "'" +
            ", hostName='" + getHostName() + "'" +
            "}";
    }
}
