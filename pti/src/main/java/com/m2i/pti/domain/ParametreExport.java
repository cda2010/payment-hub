package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.TypeChamps;
import com.m2i.pti.domain.enumeration.TypeListe;
import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A PrametreExport.
 */
@Entity
@Table(name = "parametre_export")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ParametreExport implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "nom_parametre")
  private String nomParametre;

  @Column(name = "nom_champs")
  private String nomChamps;

  @Column(name = "valeur")
  private String valeur;

  @Column(name = "cle")
  private String cle;

  @Enumerated(EnumType.STRING)
  @Column(name = "type_champs")
  private TypeChamps typeChamps;

  @Enumerated(EnumType.STRING)
  @Column(name = "type_liste")
  private TypeListe typeListe;

  @ManyToOne
  public ExportTable exportTable;

  @Column(name = "table_ref")
  private String tableRef;

  // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNomParametre() {
    return this.nomParametre;
  }

  public void setNomParametre(String nomParametre) {
    this.nomParametre = nomParametre;
  }

  public String getNomChamps() {
    return this.nomChamps;
  }

  public void setNomChamps(String nomChamps) {
    this.nomChamps = nomChamps;
  }

  public String getValeur() {
    return this.valeur;
  }

  public void setValeur(String valeur) {
    this.valeur = valeur;
  }

  public String getCle() {
    return this.cle;
  }

  public void setCle(String cle) {
    this.cle = cle;
  }

  public TypeChamps getTypeChamps() {
    return this.typeChamps;
  }

  public void setTypeChamps(TypeChamps typeChamps) {
    this.typeChamps = typeChamps;
  }

  public ExportTable getExportTable() {
    return this.exportTable;
  }

  public void setExportTable(ExportTable exportTable) {
    this.exportTable = exportTable;
  }

  public String getTableRef() {
    return tableRef;
  }

  public void setTableRef(String tableRef) {
    this.tableRef = tableRef;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ParametreExport parametreExport = (ParametreExport) o;
    if (parametreExport.getId() == null || getId() == null) {
      return false;
    }
    return Objects.equals(getId(), parametreExport.getId());
  }

  public TypeListe getTypeListe() {
    return typeListe;
  }

  public void setTypeListe(TypeListe typeListe) {
    this.typeListe = typeListe;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  @Override
  public String toString() {
    return (
      "{" +
      " id='" +
      getId() +
      "'" +
      ", nomParametre='" +
      getNomParametre() +
      "'" +
      ", nomChamps='" +
      getNomChamps() +
      "'" +
      ", valeur='" +
      getValeur() +
      "'" +
      ", cle='" +
      getCle() +
      "'" +
      ", typeChamps='" +
      getTypeChamps() +
      "'" +
      ", exportTable='" +
      getExportTable() +
      "'" +
      "}"
    );
  }
}
