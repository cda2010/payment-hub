package com.m2i.pti.service.dto;

import com.m2i.pti.domain.ClassificationFacturier;
import com.m2i.pti.domain.Facturier;
import com.m2i.pti.domain.GroupeProfilActeur;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.SecteurActivite;
import com.m2i.pti.domain.enumeration.TypeFacturier;
import java.io.Serializable;
import java.util.List;

public class FacturierDTO implements Serializable {

    private Long id;

    private String nomFacturier;

    private String codeFacturier;

    private TypeFacturier typeFacturier;

    private SecteurActivite secteurActivite;

    private UniteOrganisation possesseur;

    private UniteOrganisation uniteOrganisation;

    private ClassificationFacturier classification;

    private Long classificationId;

    private String classificationLibelle;

    private Long uniteOrganisationId;

    private boolean activated;


    private byte[] valeur;

    private String valeurContentType;

    private String marchand;
    private String possesseurLibelle;
    private Long possesseurId;

    private String secteurActiviteLibelle;
    private String typeFacturierLibelle;

    private String fse;

    private FtpDTO ftpDTO;

    
    private RecepteurEmailDTO recepteurEmailDTO;

     private List<GroupeProfilActeur> groupeProfilActeurDTOs;

     private String typeActeur;

    public FtpDTO getFtpDTO() {
        return ftpDTO;
    }

    public void setFtpDTO(FtpDTO ftpDTO) {
        this.ftpDTO = ftpDTO;
    }

    public RecepteurEmailDTO getRecepteurEmailDTO() {
        return recepteurEmailDTO;
    }

    public FacturierDTO(Facturier f){
        this.id=f.getId();
        this.nomFacturier=f.getNomFacturier();
        this.codeFacturier=f.getCodeFacturier();
        this.typeFacturier=f.getTypeFacturier();
        this.classificationLibelle=f.getClassification().getLibelleClassification();
    }

    public FacturierDTO() {
    }

    public FacturierDTO(Long id, String nomFacturier, String codeFacturier, String typeFacturierLibelle, String classificationLibelle,  String possesseurLibelle, String marchand, String fse,String secteurActiviteLibelle,boolean activated) {
        this.id = id;
        this.nomFacturier = nomFacturier;
        this.codeFacturier = codeFacturier;
        this.typeFacturierLibelle = typeFacturierLibelle;
        this.secteurActiviteLibelle = secteurActiviteLibelle;
        this.possesseurLibelle = possesseurLibelle;
        this.marchand = marchand;
        this.classificationLibelle = classificationLibelle;
        this.fse = fse;
        this.activated=activated;
    }
    
    public FacturierDTO(Long id, String nomFacturier, String codeFacturier ,String typeFacturier,Long classificationId , String classificationLibelle,  Long possesseurId,String possesseurLibelle, Long marchandId,String marchand, String fse,String secteurActivite,boolean activated) {
        this.id = id;
        this.nomFacturier = nomFacturier;
        this.codeFacturier = codeFacturier;
        if(typeFacturier!=null){
            this.typeFacturier = TypeFacturier.valueOf(typeFacturier);
        }
        if(secteurActivite!=null){
            this.secteurActivite = SecteurActivite.valueOf(secteurActivite);
        }
        this.possesseurLibelle = possesseurLibelle;
        this.possesseurId = possesseurId;
        this.marchand = marchand;
        this.uniteOrganisationId = marchandId;
        this.classificationLibelle = classificationLibelle;
        this.classificationId = classificationId;
        this.fse = fse;
        this.activated=activated;
    }

    public void setRecepteurEmailDTO(RecepteurEmailDTO recepteurEmailDTO) {
        this.recepteurEmailDTO = recepteurEmailDTO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomFacturier() {
        return nomFacturier;
    }

    public void setNomFacturier(String nomFacturier) {
        this.nomFacturier = nomFacturier;
    }

    public String getCodeFacturier() {
        return codeFacturier;
    }

    public void setCodeFacturier(String codeFacturier) {
        this.codeFacturier = codeFacturier;
    }

    public TypeFacturier getTypeFacturier() {
        return typeFacturier;
    }

    public void setTypeFacturier(TypeFacturier typeFacturier) {
        this.typeFacturier = typeFacturier;
    }

    public SecteurActivite getSecteurActivite() {
        return secteurActivite;
    }

    public void setSecteurActivite(SecteurActivite secteurActivite) {
        this.secteurActivite = secteurActivite;
    }

    public UniteOrganisation getPossesseur() {
        return possesseur;
    }

    public void setPossesseur(UniteOrganisation possesseur) {
        this.possesseur = possesseur;
    }

    public Long getClassificationId() {
        return classificationId;
    }

    public void setClassificationId(Long classificationId) {
        this.classificationId = classificationId;
    }
    

    public Long getUniteOrganisationId() {
        return uniteOrganisationId;
    }

    public void setUniteOrganisationId(Long uniteOrganisationId) {
        this.uniteOrganisationId = uniteOrganisationId;
    }

    public byte[] getValeur() {
        return valeur;
    }

    public void setValeur(byte[] valeur) {
        this.valeur = valeur;
    }

    public String getValeurContentType() {
        return valeurContentType;
    }

    public void setValeurContentType(String valeurContentType) {
        this.valeurContentType = valeurContentType;
    }

    public String getFse() {
        return fse;
    }

    public void setFse(String fse) {
        this.fse = fse;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }
    

   

    public ClassificationFacturier getClassification() {
        return classification;
    }

    public void setClassification(ClassificationFacturier classification) {
        this.classification = classification;
    }

    public String getClassificationLibelle() {
        return classificationLibelle;
    }

    public void setClassificationLibelle(String classificationLibelle) {
        this.classificationLibelle = classificationLibelle;
    }

    public String getMarchand() {
        return marchand;
    }

    public void setMarchand(String marchand) {
        this.marchand = marchand;
    }

    public String getPossesseurLibelle() {
        return possesseurLibelle;
    }

    public void setPossesseurLibelle(String possesseurLibelle) {
        this.possesseurLibelle = possesseurLibelle;
    }

    public String getSecteurActiviteLibelle() {
        return secteurActiviteLibelle;
    }

    public void setSecteurActiviteLibelle(String secteurActiviteLibelle) {
        this.secteurActiviteLibelle = secteurActiviteLibelle;
    }

    public String getTypeFacturierLibelle() {
        return typeFacturierLibelle;
    }

    public void setTypeFacturierLibelle(String typeFacturierLibelle) {
        this.typeFacturierLibelle = typeFacturierLibelle;
    }

    public Long getPossesseurId() {
        return possesseurId;
    }

    public void setPossesseurId(Long possesseurId) {
        this.possesseurId = possesseurId;
    }
    

    @Override
    public String toString() {
        return "FacturierDTO [classificationId=" + classificationId + ", codeFacturier=" + codeFacturier + ", fse="
                + fse + ", ftpDTO=" + ftpDTO + ", id=" + id + ", classification=" + classification
                + ", nomFacturier=" + nomFacturier + ", possesseur=" + possesseur + ", recepteurEmailDTO="
                + recepteurEmailDTO + ", secteurActivite=" + secteurActivite + ", typeFacturier=" + typeFacturier
                + ", uniteOrganisationId=" + uniteOrganisationId + ", valeurContentType=" + valeurContentType + "]";
    }

    public List<GroupeProfilActeur> getGroupeProfilActeurDTOs() {
        return groupeProfilActeurDTOs;
    }

    public void setGroupeProfilActeurDTOs(List<GroupeProfilActeur> groupeProfilActeurDTOs) {
        this.groupeProfilActeurDTOs = groupeProfilActeurDTOs;
    }

    public String getTypeActeur() {
        return typeActeur;
    }

    public void setTypeActeur(String typeActeur) {
        this.typeActeur = typeActeur;
    }

    

}
