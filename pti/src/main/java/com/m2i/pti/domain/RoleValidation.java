package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.Operateur;
import com.m2i.pti.service.dto.RoleValidationDTO;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@NamedNativeQueries(
  {
    @NamedNativeQuery(
      name = "RoleValidation.findAllRoleValidation",
      query = "select r.id, t.id as typeServiceId, t.libelle as typeServiceLibelle, r.operateur,r.montant, roleName from role_validation r, type_service t, jhi_authority ja where r.type_service_id = t.id and r.role=ja.id",
      resultSetMapping = "Mapping.RoleValidationDTO",
      hints = { @QueryHint(name = "org.hibernate.cacheable", value = "false") }
    ),
  }
)
@SqlResultSetMappings(
  {
    @SqlResultSetMapping(
      name = "Mapping.RoleValidationDTO",
      classes = @ConstructorResult(
        targetClass = RoleValidationDTO.class,
        columns = {
          @ColumnResult(name = "id", type = Long.class),
          @ColumnResult(name = "typeServiceId", type = String.class),
          @ColumnResult(name = "typeServiceLibelle", type = String.class),
          @ColumnResult(name = "operateur", type = String.class),
          @ColumnResult(name = "montant", type = String.class),
          @ColumnResult(name = "roleName", type = Boolean.class),
        }
      )
    ),
  }
)
/**
 * A RoleValidation.
 */
@Entity
@Table(name = "role_validation")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class RoleValidation
  extends AbstractAuditingEntity
  implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private TypeService typeService;

  @Enumerated(EnumType.STRING)
  @Column(name = "operateur")
  private Operateur operateur;

  @Column(name = "montant", precision = 10, scale = 2)
  private BigDecimal montant;

  @ManyToOne
  private Authority role;

  // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public TypeService getTypeService() {
    return typeService;
  }

  public RoleValidation typeService(TypeService typeService) {
    this.typeService = typeService;
    return this;
  }

  public void setTypeService(TypeService typeService) {
    this.typeService = typeService;
  }

  public Operateur getOperateur() {
    return operateur;
  }

  public RoleValidation operateur(Operateur operateur) {
    this.operateur = operateur;
    return this;
  }

  public void setOperateur(Operateur operateur) {
    this.operateur = operateur;
  }

  public BigDecimal getMontant() {
    return montant;
  }

  public RoleValidation montant(BigDecimal montant) {
    this.montant = montant;
    return this;
  }

  public void setMontant(BigDecimal montant) {
    this.montant = montant;
  }

  public Authority getRole() {
    return role;
  }

  public RoleValidation role(Authority role) {
    this.role = role;
    return this;
  }

  public void setRole(Authority role) {
    this.role = role;
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
    RoleValidation roleValidation = (RoleValidation) o;
    if (roleValidation.getId() == null || getId() == null) {
      return false;
    }
    return Objects.equals(getId(), roleValidation.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  @Override
  public String toString() {
    return (
      "RoleValidation{" +
      "id=" +
      getId() +
      ", typeService='" +
      getTypeService() +
      "'" +
      ", operateur='" +
      getOperateur() +
      "'" +
      ", montant='" +
      getMontant() +
      "'" +
      ", role='" +
      getRole() +
      "'" +
      "}"
    );
  }
}
