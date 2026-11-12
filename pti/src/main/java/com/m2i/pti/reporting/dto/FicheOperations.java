package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;
import java.util.List;

public class FicheOperations {
    private List<XOperationDTO> infosOperations;
    private List<XCumulOperation> infosStatistiques;
    private List<XCumulOperation> graphicMontant;
    private List<XCumulOperation> infoAnalyseMontant;
    private List<XCumulOperation> infoAnalyseNombre;
    private List<XCumulOperation> graphicNombre;
    private List<XOperationAgent> infos;
    private String titre;
    private Double total;
    private String service;
    private String uoEmetteur;
    private String uoAcquereur;
    private String periode;
    private String organisation;
    private String reseau;
    private String fse;
    private BigDecimal totalMontant;
    private String logo;

    public BigDecimal getTotalMontant() {
        return totalMontant;
    }

    public void setTotalMontant(BigDecimal totalMontant) {
        this.totalMontant = totalMontant;
    }

    public String getFse() {
        return fse;
    }

    public void setFse(String fse) {
        this.fse = fse;
    }

    public List<XCumulOperation> getGraphicMontant() {
        return graphicMontant;
    }

    public void setGraphicMontant(List<XCumulOperation> graphicMontant) {
        this.graphicMontant = graphicMontant;
    }

    public List<XCumulOperation> getInfoAnalyseMontant() {
        return infoAnalyseMontant;
    }

    public void setInfoAnalyseMontant(List<XCumulOperation> infoAnalyseMontant) {
        this.infoAnalyseMontant = infoAnalyseMontant;
    }

    public List<XCumulOperation> getInfoAnalyseNombre() {
        return infoAnalyseNombre;
    }

    public void setInfoAnalyseNombre(List<XCumulOperation> infoAnalyseNombre) {
        this.infoAnalyseNombre = infoAnalyseNombre;
    }

    public List<XCumulOperation> getGraphicNombre() {
        return graphicNombre;
    }

    public void setGraphicNombre(List<XCumulOperation> graphicNombre) {
        this.graphicNombre = graphicNombre;
    }

    public List<XCumulOperation> getInfosStatistiques() {
        return infosStatistiques;
    }

    public void setInfosStatistiques(List<XCumulOperation> infosStatistiques) {
        this.infosStatistiques = infosStatistiques;
    }

    public String getReseau() {
        return reseau;
    }

    public void setReseau(String reseau) {
        this.reseau = reseau;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getUoEmetteur() {
        return uoEmetteur;
    }

    public void setUoEmetteur(String uoEmetteur) {
        this.uoEmetteur = uoEmetteur;
    }

    public String getUoAcquereur() {
        return uoAcquereur;
    }

    public void setUoAcquereur(String uoAcquereur) {
        this.uoAcquereur = uoAcquereur;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public List<XOperationDTO> getInfosOperations() {
        return infosOperations;
    }

    public void setInfosOperations(List<XOperationDTO> infosOperations) {
        this.infosOperations = infosOperations;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<XOperationAgent> getInfos() {
        return infos;
    }

    public void setInfos(List<XOperationAgent> infos) {
        this.infos = infos;
    }
}
