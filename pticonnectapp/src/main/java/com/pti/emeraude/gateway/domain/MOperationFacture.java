package com.pti.emeraude.gateway.domain;

import java.math.BigDecimal;
import java.util.List;


public class MOperationFacture {
    private String codePv;
    private BigDecimal montant;
    private BigDecimal montantFrais;
    private BigDecimal montantTaxe;
    private BigDecimal montantFraisClient;
 private Long nombre;
    private String canal;
    private String typeService;
    private String typeServiceConditionel;
    private String motif;
    private String codeUo;
   private String referenceFacture;
    private MInfos emetteur;
  private MProduit produit;
    private MFacturier facturier;
    private String codePays;//Pays du paiement
    private MInfos beneficiaire;
  private String userConnecte;
     private String identifiant;
    private String acteur;
     List<MParametre> listParam;
      private String application;
    public String getCodePays() {
        return codePays;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getActeur() {
        return acteur;
    }

    public void setActeur(String acteur) {
        this.acteur = acteur;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public String getCodePv() {
        return codePv;
    }

    public void setCodePv(String codePv) {
        this.codePv = codePv;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }

    public MInfos getEmetteur() {
        return emetteur;
    }

    public void setEmetteur(MInfos emetteur) {
        this.emetteur = emetteur;
    }

    public MInfos getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(MInfos beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public BigDecimal getMontantFrais() {
        return montantFrais;
    }

    public void setMontantFrais(BigDecimal montantFrais) {
        this.montantFrais = montantFrais;
    }

    public BigDecimal getMontantTaxe() {
        return montantTaxe;
    }

    public void setMontantTaxe(BigDecimal montantTaxe) {
        this.montantTaxe = montantTaxe;
    }

    public String getUserConnecte() {
        return userConnecte;
    }

    public void setUserConnecte(String userConnecte) {
        this.userConnecte = userConnecte;
    }

    public String getReferenceFacture() {
        return referenceFacture;
    }

    public void setReferenceFacture(String referenceFacture) {
        this.referenceFacture = referenceFacture;
    }

    public Long getNombre() {
        return nombre;
    }

    public void setNombre(Long nombre) {
        this.nombre = nombre;
    }

    public MProduit getProduit() {
        return produit;
    }

    public void setProduit(MProduit produit) {
        this.produit = produit;
    }

    public MFacturier getFacturier() {
        return facturier;
    }

    public void setFacturier(MFacturier facturier) {
        this.facturier = facturier;
    }

    public List<MParametre> getListParam() {
        return listParam;
    }

    public void setListParam(List<MParametre> listParam) {
        this.listParam = listParam;
    }

    public BigDecimal getMontantFraisClient() {
        return montantFraisClient;
    }

    public void setMontantFraisClient(BigDecimal montantFraisClient) {
        this.montantFraisClient = montantFraisClient;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getTypeServiceConditionel() {
        return typeServiceConditionel;
    }

    public void setTypeServiceConditionel(String typeServiceConditionel) {
        this.typeServiceConditionel = typeServiceConditionel;
    }
}
