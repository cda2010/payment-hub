package com.m2i.pti.domain;

import com.m2i.pti.service.dto.AutorisationDTO;
import com.m2i.pti.service.dto.PointVenteDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@NamedNativeQuery(name = "Autorisation.findByCondition",
        query = "SELECT id,code, unite_organisation_id uniteOrganisationId, type_service_id typeServiceId,  "
                + "client_id clientId from autorisation a WHERE unite_organisation_id=:uoId and type_service_id=:typeserviceId",
        resultSetMapping = "Mapping.AutorisationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})

@SqlResultSetMapping(name = "Mapping.AutorisationDTO",
        classes = @ConstructorResult(targetClass = AutorisationDTO.class,
                columns = {
                    @ColumnResult(name = "id", type = Long.class),
                    @ColumnResult(name = "code", type = String.class),
                    @ColumnResult(name = "uniteOrganisationId", type = Long.class),
                    @ColumnResult(name = "typeServiceId", type = Long.class),
                    @ColumnResult(name = "clientId", type = Long.class)
                }))

/**
 * A Autorisation.
 */
@Entity
@Table(name = "autorisation")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Autorisation extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @ManyToOne
    private UniteOrganisation uniteOrganisation;

    @ManyToOne
    private TypeService typeService;

    @ManyToOne
    private User utilisateur;

    @ManyToOne
    private Client client;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public Autorisation code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public Autorisation uniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
        return this;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public Autorisation typeService(TypeService typeService) {
        this.typeService = typeService;
        return this;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public User getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(User utilisateur) {
        this.utilisateur = utilisateur;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
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
        Autorisation autorisation = (Autorisation) o;
        if (autorisation.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), autorisation.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Autorisation{"
                + "id=" + getId()
                + ", code='" + getCode() + "'"
                + "}";
    }
}
