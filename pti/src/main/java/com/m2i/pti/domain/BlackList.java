package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.ActionBlackList;
import com.m2i.pti.service.dto.BlackListDTO;
import com.m2i.pti.service.dto.OperationSuivisDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A BlackList.
 */
@NamedNativeQueries({
        @NamedNativeQuery(name = "BlackList.findAll",
                query = "select b.id, b.client_id clientId,ph.nom_client,ph.prenom_client,p.telephone,ph.nom_mere_client,b.action_black_list as actionBlack,b.created_by as createdBy,b.created_date as createdDate,b.supprime as supprime "
                        + "from black_list b ,client c,personne p,personne_physique ph where b.client_id=c.id and c.personne_id=p.id\n" +
                        "and p.personne_physique_id=ph.id  and\n " +
                        "(:telephone is null or p.telephone like UPPER('%'||CAST (:telephone AS TEXT)||'%'))\n" +
                        "and (:nomClient is null or ph.nom_client like  UPPER('%'||CAST (:nomClient AS TEXT)||'%') ) and (:prenomClient is null or ph.prenom_client like  UPPER('%'||CAST (:prenomClient AS TEXT)||'%')) and (:actionBlackList is null or b.action_black_list like  UPPER('%'||CAST (:actionBlackList AS TEXT)||'%')) and b.supprime=false limit :taille offset :page ",
                resultSetMapping = "Mapping.BlackListDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})
        ,
})
@SqlResultSetMappings({
        @SqlResultSetMapping(name = "Mapping.BlackListDTO",
                classes = @ConstructorResult(targetClass = BlackListDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class)
                                ,
                                @ColumnResult(name = "clientId", type = Long.class)
                                ,
                                @ColumnResult(name = "actionBlack", type = String.class),
                                @ColumnResult(name = "createdBy", type = String.class),
                                @ColumnResult(name = "createdDate", type = Instant.class),
                                @ColumnResult(name = "supprime", type = Boolean.class),
                        })),

})


@Entity
@Table(name = "black_list")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class BlackList extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public ActionBlackList getActionBlackList() {
        return actionBlackList;
    }

    public void setActionBlackList(ActionBlackList actionBlackList) {
        this.actionBlackList = actionBlackList;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "actionBlackList")
    private ActionBlackList actionBlackList;

    @ManyToOne
    private Client client;

    @Column(name = "supprime")
    private Boolean supprime = false;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public BlackList client(Client client) {
        this.client = client;
        return this;
    }

    public void setClient(Client client) {
        this.client = client;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove


    public Boolean getSupprime() {
        return supprime;
    }

    public void setSupprime(Boolean supprime) {
        this.supprime = supprime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlackList blackList = (BlackList) o;
        if (blackList.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), blackList.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "BlackList{" +
            "id=" + getId() +
            "}";
    }
}
