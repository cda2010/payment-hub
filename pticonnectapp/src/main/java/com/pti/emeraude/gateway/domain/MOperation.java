package com.pti.emeraude.gateway.domain;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlElement;

public class MOperation extends StandardResponse {

    private String deviseSrc;
    private String deviseDest;
    private String codePv;
    private BigDecimal montant;
    private BigDecimal montantEnDevise;

    private BigDecimal montantFrais;
    private BigDecimal montantTaxe;
    private String canal;
    private Boolean cardLess;
    private String typeService;
    private BigDecimal montantFraisClient;
    private String responsableFrais;
    private String motif;
    private String identifiant;
    private String acteur;
    private String codeUo;
    private String codeUoEm;
    private String codePvEm;
    private MInfos emetteur;
    private String codeUoAcq;
    private MInfos beneficiaire;

    private String codeRetrait;

    private String userConnecte;
    private String codePays;//Pays du paiement
    private String description;
    private String application;

    private String rtgs;

    private Integer preautorisaton;
    
    
    private String etape;
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

    public String getCodeRetrait() {
        return codeRetrait;
    }

    public void setCodeRetrait(String codeRetrait) {
        this.codeRetrait = codeRetrait;
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

    public Boolean getCardLess() {
        return cardLess;
    }

    public void setCardLess(Boolean cardLess) {
        this.cardLess = cardLess;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResponsableFrais() {
        return responsableFrais;
    }

    public void setResponsableFrais(String responsableFrais) {
        this.responsableFrais = responsableFrais;
    }

    public String getCodeUoEm() {
        return codeUoEm;
    }

    public void setCodeUoEm(String codeUoEm) {
        this.codeUoEm = codeUoEm;
    }

    public String getCodePvEm() {
        return codePvEm;
    }

    public void setCodePvEm(String codePvEm) {
        this.codePvEm = codePvEm;
    }

    public String getCodeUoAcq() {
        return codeUoAcq;
    }

    public void setCodeUoAcq(String codeUoAcq) {
        this.codeUoAcq = codeUoAcq;
    }

    public BigDecimal getMontantFraisClient() {
        return montantFraisClient;
    }

    public void setMontantFraisClient(BigDecimal montantFraisClient) {
        this.montantFraisClient = montantFraisClient;
    }

    public String getDeviseSrc() {
        return deviseSrc;
    }

    public void setDeviseSrc(String deviseSrc) {
        this.deviseSrc = deviseSrc;
    }

    public String getDeviseDest() {
        return deviseDest;
    }

    public void setDeviseDest(String deviseDest) {
        this.deviseDest = deviseDest;
    }

    public BigDecimal getMontantEnDevise() {
        return montantEnDevise;
    }

    public void setMontantEnDevise(BigDecimal montantEnDevise) {
        this.montantEnDevise = montantEnDevise;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getRtgs() {
        return rtgs;
    }

    public void setRtgs(String rtgs) {
        this.rtgs = rtgs;
    }

    public Integer getPreautorisaton() {
        return preautorisaton;
    }

    public void setPreautorisaton(Integer preautorisaton) {
        this.preautorisaton = preautorisaton;
    }

    public String getEtape() {
        return etape;
    }

    public void setEtape(String etape) {
        this.etape = etape;
    }

   

}
