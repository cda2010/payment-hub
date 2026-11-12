package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.Operateur;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the RoleValidation entity.
 */
public class RoleValidationDTO implements Serializable {
  private Long id;

  private Long typeServiceId;
  private String typeServiceLibelle;
  private Operateur operateur;
  private BigDecimal montant;

  private String roleName;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getTypeServiceId() {
    return typeServiceId;
  }

  public void setTypeServiceId(Long typeServiceId) {
    this.typeServiceId = typeServiceId;
  }

  public Operateur getOperateur() {
    return operateur;
  }

  public void setOperateur(Operateur operateur) {
    this.operateur = operateur;
  }

  public BigDecimal getMontant() {
    return montant;
  }

  public void setMontant(BigDecimal montant) {
    this.montant = montant;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public String getTypeServiceLibelle() {
    return typeServiceLibelle;
  }

  public void setTypeServiceLibelle(String typeServiceLibelle) {
    this.typeServiceLibelle = typeServiceLibelle;
  }

  public RoleValidationDTO() {}

  public RoleValidationDTO(
    Long id,
    Long typeServiceId,
    String typeServiceLibelle,
    String operateur,
    BigDecimal montant,
    String roleName
  ) {
    this.id = id;
    this.typeServiceId = typeServiceId;
    this.typeServiceLibelle = typeServiceLibelle;
    this.operateur = operateur != null ? Operateur.valueOf(operateur) : null;
    this.montant = montant;
    this.roleName = roleName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RoleValidationDTO roleValidationDTO = (RoleValidationDTO) o;
    if (roleValidationDTO.getId() == null || getId() == null) {
      return false;
    }
    return Objects.equals(getId(), roleValidationDTO.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  @Override
  public String toString() {
    return (
      "RoleValidationDTO{" +
      "id=" +
      getId() +
      ", typeService='" +
      getTypeServiceId() +
      "'" +
      ", operateur='" +
      getOperateur() +
      "'" +
      ", montant='" +
      getMontant() +
      "'" +
      ", role='" +
      getRoleName() +
      "'" +
      "}"
    );
  }
}
