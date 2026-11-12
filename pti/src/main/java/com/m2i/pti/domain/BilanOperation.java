package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.service.dto.BilanOperationDTO;
import com.m2i.pti.service.dto.ClientDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@SqlResultSetMapping(name = "Mapping.BilanOperationDTO",
        classes = @ConstructorResult(targetClass = BilanOperationDTO.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class)
                        ,
                        @ColumnResult(name = "dateDerniereOperation", type = LocalDate.class)
                        ,
                        @ColumnResult(name = "montant", type = BigDecimal.class)
                        ,
                        @ColumnResult(name = "nombre", type = Integer.class)
                        ,
                        @ColumnResult(name = "typeServiceLibelle", type = String.class)

                }))

@NamedNativeQueries({

        @NamedNativeQuery(name = "BilanOperation.findBilanOpByPersonneIdDate",
                query = "select b.id, b.date_derniere_operation as dateDerniereOperation , b.montant as montant, b. nombre as nombre, t.code as  typeServiceLibelle \n"
                        + " from bilan_operation b,type_service t where b.type_service_id=t.id and (b.client_emetteur_id = :personneId or b.client_beneficiaire_id = :personneId) and ((cast(:debut  as timestamp) is null or cast(:debut  as timestamp) is null ) or (b.date_derniere_operation between :debut and  :fin) ) order by b.date_derniere_operation desc limit :taille offset :page",
                resultSetMapping = "Mapping.BilanOperationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")}),

        @NamedNativeQuery(name = "BilanOperation.findBilanOpByPersonneId",
                query = "select b.id, b.date_derniere_operation as dateDerniereOperation , b.montant as montant, b. nombre as nombre, t.code as  typeServiceLibelle \n"
                        + " from bilan_operation b,type_service t where b.type_service_id=t.id and (b.client_emetteur_id = :personneId or b.client_beneficiaire_id = :personneId)  order by b.date_derniere_operation desc limit :taille offset :page",
                resultSetMapping = "Mapping.BilanOperationDTO",hints={@QueryHint(name="org.hibernate.cacheable",value="false")})

})
/**
 * A BilanOperation.
 */
@Entity
@Table(name = "bilan_operation")

public class BilanOperation extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;
 @Transient
    @JsonIgnore
    private final Object lock = new Object();
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private Integer nombre;

    @Column(name = "montant")
    private BigDecimal montant;

    @Column(name = "date_derniere_operation")
    private LocalDate dateDerniereOperation;

    @ManyToOne
    private TypeService typeService;

    @ManyToOne
    private Canal canal;

    @ManyToOne
    private Client beneficiaire;

    @ManyToOne
    private Client emetteur;

    @ManyToOne
    private Personne clientBeneficiaire;

    @ManyToOne
    private Personne clientEmetteur;


    @ManyToOne
    private UniteOrganisation organisationEm;

    @ManyToOne
    private UniteOrganisation organisationAcq;
/* l'identité de l'acteur qui est en charge de l'opération */
    @ManyToOne
    private UniteOrganisation acteur;


    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Personne getClientBeneficiaire() {
        return clientBeneficiaire;
    }

    public void setClientBeneficiaire(Personne clientBeneficiaire) {
        this.clientBeneficiaire = clientBeneficiaire;
    }

    public Personne getClientEmetteur() {
        return clientEmetteur;
    }

    public void setClientEmetteur(Personne clientEmetteur) {
        this.clientEmetteur = clientEmetteur;
    }

    public Integer getNombre() {
        return nombre;
    }

    public BilanOperation nombre(Integer nombre) {
        this.nombre = nombre;
        return this;
    }

    public void setNombre(Integer nombre) {
        
         synchronized (lock) {
             this.nombre = nombre;
        }
       
    }

    public BigDecimal getMontant() {
          synchronized (lock) {
               return montant;
          }
       
    }

    public BilanOperation montant(BigDecimal montant) {
        synchronized (lock) {
            this.montant = montant;
            return this;
        }
        
    }

    public void setMontant(BigDecimal montant) {
          synchronized (lock) {
               this.montant = montant;
          }
       
    }

    public LocalDate getDateDerniereOperation() {
        return dateDerniereOperation;
    }

    public BilanOperation dateDerniereOperation(LocalDate dateDerniereOperation) {
        this.dateDerniereOperation = dateDerniereOperation;
        return this;
    }

    public void setDateDerniereOperation(LocalDate dateDerniereOperation) {
        this.dateDerniereOperation = dateDerniereOperation;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public BilanOperation typeService(TypeService typeService) {
        this.typeService = typeService;
        return this;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public Canal getCanal() {
        return canal;
    }

    public BilanOperation canal(Canal canal) {
        this.canal = canal;
        return this;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public Client getBeneficiaire() {
        return beneficiaire;
    }

    public BilanOperation beneficiaire(Client client) {
        this.beneficiaire = client;
        return this;
    }

    public void setBeneficiaire(Client client) {
        this.beneficiaire = client;
    }

    public Client getEmetteur() {
        return emetteur;
    }

    public BilanOperation emetteur(Client client) {
        this.emetteur = client;
        return this;
    }

    public void setEmetteur(Client client) {
        this.emetteur = client;
    }

    public UniteOrganisation getOrganisationEm() {
        return organisationEm;
    }

    public BilanOperation organisationEm(UniteOrganisation uniteOrganisation) {
        this.organisationEm = uniteOrganisation;
        return this;
    }

    public void setOrganisationEm(UniteOrganisation uniteOrganisation) {
        this.organisationEm = uniteOrganisation;
    }

    public UniteOrganisation getOrganisationAcq() {
        return organisationAcq;
    }

    public BilanOperation organisationAcq(UniteOrganisation uniteOrganisation) {
        this.organisationAcq = uniteOrganisation;
        return this;
    }

    public void setOrganisationAcq(UniteOrganisation uniteOrganisation) {
        this.organisationAcq = uniteOrganisation;
    }

    public UniteOrganisation getActeur() {
        return acteur;
    }

    public BilanOperation acteur(UniteOrganisation uniteOrganisation) {
        this.acteur = uniteOrganisation;
        return this;
    }

    public void setActeur(UniteOrganisation uniteOrganisation) {
        this.acteur = uniteOrganisation;
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
        BilanOperation bilanOperation = (BilanOperation) o;
        if (bilanOperation.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), bilanOperation.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "BilanOperation{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", montant='" + getMontant() + "'" +
            ", dateDerniereOperation='" + getDateDerniereOperation() + "'" +
            "}";
    }
}
