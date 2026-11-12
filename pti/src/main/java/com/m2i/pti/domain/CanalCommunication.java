package com.m2i.pti.domain;



import com.m2i.pti.service.dto.CanalCommunicationDTO;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
@NamedNativeQueries({
@NamedNativeQuery(name = "CanalCommunication.findCanalCommunication",
        query = "select c.id, c. facturier_id facturierId ,ftp_id ftpId ,recepteur_email_id recepteurEmailId ,fse  from canal_communication c, facturier fac where fac.id= c.facturier_id",
        resultSetMapping = "Mapping.CanalCommunicationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
})

@SqlResultSetMapping(name = "Mapping.CanalCommunicationDTO",
        classes = @ConstructorResult(targetClass = CanalCommunicationDTO.class,
                columns = {
                    @ColumnResult(name = "id", type = Long.class),
                    @ColumnResult(name = "facturierId", type = Long.class),
                    @ColumnResult(name = "ftpId", type = Long.class),
                    @ColumnResult(name = "recepteurEmailId", type = Long.class),
                    @ColumnResult(name = "fse", type = String.class)
                   
                }))
/**
 * A CanalCommunication.
 */
@Entity
@Table(name = "canal_communication")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CanalCommunication implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(unique = true)
    private Facturier facturier;

    @OneToOne
    @JoinColumn(unique = true)
    private Ftp ftp;

    @OneToOne
    @JoinColumn(unique = true)
    private RecepteurEmail recepteurEmail;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Facturier getFacturier() {
        return facturier;
    }

    public CanalCommunication facturier(Facturier facturier) {
        this.facturier = facturier;
        return this;
    }

    public void setFacturier(Facturier facturier) {
        this.facturier = facturier;
    }

    public Ftp getFtp() {
        return ftp;
    }

    public CanalCommunication ftp(Ftp ftp) {
        this.ftp = ftp;
        return this;
    }

    public void setFtp(Ftp ftp) {
        this.ftp = ftp;
    }

    public RecepteurEmail getRecepteurEmail() {
        return recepteurEmail;
    }

    public CanalCommunication recepteurEmail(RecepteurEmail recepteurEmail) {
        this.recepteurEmail = recepteurEmail;
        return this;
    }

    public void setRecepteurEmail(RecepteurEmail recepteurEmail) {
        this.recepteurEmail = recepteurEmail;
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
        CanalCommunication canalCommunication = (CanalCommunication) o;
        if (canalCommunication.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), canalCommunication.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CanalCommunication{" +
            "id=" + getId() +
            "}";
    }
}
