package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.config.audit.EntityAuditEventListener;
import com.m2i.pti.service.dto.MachineDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Base64;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A Machine.
 */
@SqlResultSetMappings(value = {
        @SqlResultSetMapping(name = "Mapping.MachineDTO",
                classes = @ConstructorResult(targetClass = MachineDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "uuid", type = String.class),
                                @ColumnResult(name = "hostName", type = String.class),
                                @ColumnResult(name = "currentUser", type = String.class),
                                @ColumnResult(name = "macAddress", type = String.class),
                                @ColumnResult(name = "hostId", type = String.class),
                                @ColumnResult(name = "computerManufacturer", type = String.class),
                                @ColumnResult(name = "computerSerialNumber", type = String.class),
                                @ColumnResult(name = "computerModel", type = String.class),
                                @ColumnResult(name = "firwareManufacturer", type = String.class),
                                @ColumnResult(name = "firwareName", type = String.class),
                                @ColumnResult(name = "firwareDescription", type = String.class),
                                @ColumnResult(name = "processorIdentifier", type = String.class),
                                @ColumnResult(name = "processorId", type = String.class),
                                @ColumnResult(name = "networkInterfaces", type = String.class),
                                @ColumnResult(name = "networkParameters", type = String.class),
                                @ColumnResult(name = "enabled", type = Boolean.class),
                                @ColumnResult(name = "emailCertificat", type = String.class),
                                @ColumnResult(name = "emailCodeAcces", type = String.class),
                                @ColumnResult(name = "tempPass", type = String.class),
                                @ColumnResult(name = "adresseIp", type = String.class),
                                @ColumnResult(name = "detailsMachine", type = Base64.class),
                                @ColumnResult(name = "detailsMachineContentType", type = String.class),
                                @ColumnResult(name = "organisationId", type = Long.class),
                                @ColumnResult(name = "libelleUniteOrganisation", type = String.class)
                        }))
})

@NamedNativeQueries({
        @NamedNativeQuery(name = "Machine.findAllByCriteria",
                query = "select m.id, m.uuid, m.host_name as hostName, m.current_system_user as currentUser, m.mac_address as macAddress," +
                        "m.host_id as hostId, m.computer_manufacturer as computerManufacturer, m.computer_serial_number as computerSerialNumber," +
                        "m.computer_model as computerModel, m.firware_manufacturer as firwareManufacturer, m.firware_name as firwareName," +
                        "m.firware_description as firwareDescription, m.processor_identifier as processorIdentifier, m.processor_id as processorId," +
                        "m.network_interfaces as networkInterfaces, m.network_parameters as networkParameters, m.enabled, m.email_certificat as emailCertificat," +
                        "m.email_code_acces as emailCodeAcces, m.temp_pass as tempPass, m.adresse_ip as adresseIp, m.details_machine as detailsMachine," +
                        "m.details_machine_content_type as detailsMachineContentType," +
                        "m.organisation_id as organisationId, uo.libelle_unite_organisation as libelleUniteOrganisation" +
                        " from machine m, unite_organisation uo" +
                        " where (" +
                        "(m.organisation_id=uo.id)" +
                        "and (:orgId is null or m.organisation_id= CAST (CAST (:orgId as text) as BIGINT))" +
                        "and (:adresseIp is null or :adresseIp='' or m.adresse_ip like('%'|| CAST(:adresseIp as text)||'%'))" +
                        "and (m.supprime is null or m.supprime=false)) order by m.id desc limit :taille offset :page",
                resultSetMapping = "Mapping.MachineDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")} )
})



@Entity
@Table(name = "machine")
@EntityListeners(value = {EntityAuditEventListener.class})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Machine extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "host_name")
    private String hostName;

    @Column(name = "current_system_user", length = 50)
    private String currentUser;

    @Column(name = "mac_address", length = 50)
    private String macAddress;

    @Column(name = "host_id")
    private String hostId;

    @Column(name = "computer_manufacturer", length = 50)
    private String computerManufacturer;

    @Column(name = "computer_serial_number", length = 50)
    private String computerSerialNumber;

    @Column(name = "computer_model", length = 50)
    private String computerModel;

    @Column(name = "firware_manufacturer", length = 50)
    private String firwareManufacturer;

    @Column(name = "firware_name", length = 50)
    private String firwareName;

    @Column(name = "firware_description")
    private String firwareDescription;

    @Column(name = "processor_identifier")
    private String processorIdentifier;

    @Column(name = "processor_id", length = 20)
    private String processorId;

    @Column(name = "network_interfaces", length = 1024)
    private String networkInterfaces;

    @Column(name = "network_parameters")
    private String networkParameters;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "emailCertificat")
    private String emailCertificat;

    @Column(name = "emailCodeAcces")
    private String emailCodeAcces;

    @Column(name = "tempPass")
    private String tempPass;

    @Column(name = "adresseIp")
    private String adresseIp;

    @Lob
    @Column(name = "detailsMachine")
    private byte[] detailsMachine;

    @Column(name = "details_machine_contentType")
    private String detailsMachineContentType;

    @Column(name="supprime")
    private Boolean supprime=false;

    @ManyToOne
    private UniteOrganisation organisation;

    @OneToMany(mappedBy = "machine")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<MachineAuditEvent> audits = new HashSet<>();

    public UniteOrganisation getOrganisation() {
        return organisation;
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

    public void setOrganisation(UniteOrganisation organisation) {
        this.organisation = organisation;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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

    public Machine uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getHostName() {
        return hostName;
    }

    public Machine hostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Boolean getSupprime() {
        return supprime;
    }

    public void setSupprime(Boolean supprime) {
        this.supprime = supprime;
    }

    public Set<MachineAuditEvent> getAudits() {
        return audits;
    }

    public void setAudits(Set<MachineAuditEvent> audits) {
        this.audits = audits;
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
        Machine machine = (Machine) o;
        if (machine.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), machine.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Machine{" +
            "id=" + getId() +
            ", uuid='" + getUuid() + "'" +
            ", hostName='" + getHostName() + "'" +
                ", adresseIp='" + getAdresseIp() + "'" +
            "}";
    }
}
