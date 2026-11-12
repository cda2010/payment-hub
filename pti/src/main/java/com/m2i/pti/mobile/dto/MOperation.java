package com.m2i.pti.mobile.dto;

import java.math.BigDecimal;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
@XmlRootElement(name = "COMMAND")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(String.class)
public class MOperation {

     @XmlElement(name = "DEVISESRC")
    private String deviseSrc;
    @XmlElement(name = "DEVISEDEST")
    private String deviseDest;
    @XmlElement(name = "CODEPV")
    private String codePv;
    @XmlElement(name = "MONTANT")
    private BigDecimal montant;
    @XmlElement(name = "MONTANTENDEVISE")
    private BigDecimal montantEnDevise;
    @XmlElement(name = "MONTANTFRAIS")
    private BigDecimal montantFrais;
    @XmlElement(name = "MONTANTTAXE")
    private BigDecimal montantTaxe;
    @XmlElement(name = "REPONSABLEFRAIS")
    private String responsableFrais;
    @XmlElement(name = "MONTANTFRAISCLIENT")
    private BigDecimal montantFraisClient;

    @XmlElement(name = "MONTANTAVOIRCLIENT")
    private BigDecimal montantAvoirClient;

    @XmlElement(name = "CANAL")
    private String canal;
    @XmlElement(name = "TYPESERVICE")
    private String typeService;
    @XmlElement(name = "MOTIF")
    private String motif;
    @XmlElement(name = "CODEUO")
    private String codeUo;
    @XmlElement(name = "CODEUOEM")
    private String codeUoEm;
    @XmlElement(name = "CODEUOACQ")
    private String codeUoAcq;
    @XmlElement(name = "CODEPVEM")
    private String codePvEm;
    @XmlElement(name = "EMETEUR")
    private MInfos emetteur;
    @XmlElement(name = "CODEPAYS")
    private String codePays;//Pays du paiement
    @XmlElement(name = "CODERETRAIT")
    private String codeRetrait;
    @XmlElement(name = "BENEFICIAIRE")
    private MInfos beneficiaire;
    @XmlElement(name = "CARDLESS")
    private Boolean cardLess;
    @XmlElement(name = "USERCONNECTE")
    private String userConnecte;
    @XmlElement(name = "IDENTIFIANT")
    private String identifiant;
    @XmlElement(name = "ACTEUR")
    private String acteur;
    @XmlElement(name = "DESCRIPTION")
    private String description;
    @XmlElement(name = "APPLICATION")
    private String application;

    @XmlElement(name = "PREAUTORISATION")
    private Integer preautorisaton;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Boolean getCardLess() {
        return cardLess;
    }

    public void setCardLess(Boolean cardLess) {
        this.cardLess = cardLess;
    }

    public String getCodeRetrait() {
        return codeRetrait;
    }

    public void setCodeRetrait(String codeRetrait) {
        this.codeRetrait = codeRetrait;
    }

    public String getCodePays() {
        return codePays;
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

    public BigDecimal getMontantAvoirClient() {
        return montantAvoirClient;
    }

    public void setMontantAvoirClient(BigDecimal montantAvoirClient) {
        this.montantAvoirClient = montantAvoirClient;
    }

    public Integer getPreautorisaton() {
        return preautorisaton;
    }

    public void setPreautorisaton(Integer preautorisaton) {
        this.preautorisaton = preautorisaton;
    }

}
