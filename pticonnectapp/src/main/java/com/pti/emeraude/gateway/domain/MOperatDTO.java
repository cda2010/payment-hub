package com.pti.emeraude.gateway.domain;


import com.pti.emeraude.gateway.enumeration.StatutOperation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the Operation entity.
 */
public class MOperatDTO implements Serializable {

    private Long id;

    private BigDecimal montantOp;

    private BigDecimal montantFrais;

    private BigDecimal montantFraisClient;

    private BigDecimal montantFraisEm;

    private BigDecimal montantFraisAcq;

    private BigDecimal montantTaxe;

    private String dateOperation;

    private StatutOperation statut;

    private String codeRetrait;

    private String secret;

    private String codeOperation;

    private String dateExpiration;

    private String motif;

    private String codeExterne;

    private Long typeServiceId;

    private String code;

    private Long beneficaireId;

    private Long emetteurId;

    private Long pointVenteId;

    private Long paysId;

    private String paysLibellePays;

    private Long canalId;

    private String canalLibelleCanal;

    private Long uoAcquereurId;

    private Long lieuPaiementId;

    private Long uoEmetteurId;

    private Long uoBeneficiaireId;

    private BigDecimal montantOpEnDevise;

    private Long devisePaiementId;

    private String devisePaiementLibelle;

    private String fseSrc;

    private String fseDest;

    private String uoAcquereurLibelleUniteOrganisation;

    private String uoBeneficiaireLibelleUniteOrganisation;

    private String uoEmetteurLibelleUniteOrganisation;

    private String dateDepart;

    private String dateArrive;

    private Long pieceBeneficiaireId;

    private Long pieceEmetteurId;

    private String compteDestValeurCompte;

    private String compteSrcValeurCompte;

    private Long compteSrcId;

    private Long compteDestId;

    private String commentaire;

    private String dateTraitement;

    private Long userId;

    private String login;

    private String telephone;

    private String beneficiaireNum;

    private Long operationTraiteId;

    private BigDecimal total;

    private Long motifOperationId;
   
    private String createdDate;

    private String createdBy;

    public Long getPieceBeneficiaireId() {
        return pieceBeneficiaireId;
    }

    public void setPieceBeneficiaireId(Long pieceBeneficiaireId) {
        this.pieceBeneficiaireId = pieceBeneficiaireId;
    }

    public Long getPieceEmetteurId() {
        return pieceEmetteurId;
    }

    public void setPieceEmetteurId(Long pieceEmetteurId) {
        this.pieceEmetteurId = pieceEmetteurId;
    }

    public String getDevisePaiementLibelle() {
        return devisePaiementLibelle;
    }

    public void setDevisePaiementLibelle(String devisePaiementLibelle) {
        this.devisePaiementLibelle = devisePaiementLibelle;
    }

    public String getFseSrc() {
        return fseSrc;
    }

    public void setFseSrc(String fseSrc) {
        this.fseSrc = fseSrc;
    }

    public String getFseDest() {
        return fseDest;
    }

    public void setFseDest(String fseDest) {
        this.fseDest = fseDest;
    }

    public BigDecimal getMontantOpEnDevise() {
        return montantOpEnDevise;
    }

    public void setMontantOpEnDevise(BigDecimal montantOpEnDevise) {
        this.montantOpEnDevise = montantOpEnDevise;
    }

    public Long getDevisePaiementId() {
        return devisePaiementId;
    }

    public void setDevisePaiementId(Long devisePaiementId) {
        this.devisePaiementId = devisePaiementId;
    }

    public Long getLieuPaiementId() {
        return lieuPaiementId;
    }

    public void setLieuPaiementId(Long lieuPaiementId) {
        this.lieuPaiementId = lieuPaiementId;
    }

  

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getMontantTaxe() {
        return montantTaxe;
    }

    public void setMontantTaxe(BigDecimal montantTaxe) {
        this.montantTaxe = montantTaxe;
    }

   

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Long getUoEmetteurId() {
        return uoEmetteurId;
    }

    public void setUoEmetteurId(Long uoEmetteurId) {
        this.uoEmetteurId = uoEmetteurId;
    }

    public String getUoEmetteurLibelleUniteOrganisation() {
        return uoEmetteurLibelleUniteOrganisation;
    }

    public void setUoEmetteurLibelleUniteOrganisation(String uoEmetteurLibelleUniteOrganisation) {
        this.uoEmetteurLibelleUniteOrganisation = uoEmetteurLibelleUniteOrganisation;
    }

  

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMontantOp() {
        return montantOp;
    }

    public void setMontantOp(BigDecimal montantOp) {
        this.montantOp = montantOp;
    }

    public BigDecimal getMontantFrais() {
        return montantFrais;
    }

    public void setMontantFrais(BigDecimal montantFrais) {
        this.montantFrais = montantFrais;
    }

    public String getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(String dateOperation) {
        this.dateOperation = dateOperation;
    }

    public StatutOperation getStatut() {
        return statut;
    }

    public void setStatut(StatutOperation statut) {
        this.statut = statut;
    }

    public String getCodeRetrait() {
        return codeRetrait;
    }

    public void setCodeRetrait(String codeRetrait) {
        this.codeRetrait = codeRetrait;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public String getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(String dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getCodeExterne() {
        return codeExterne;
    }

    public void setCodeExterne(String codeExterne) {
        this.codeExterne = codeExterne;
    }

    public Long getTypeServiceId() {
        return typeServiceId;
    }

    public void setTypeServiceId(Long typeServiceId) {
        this.typeServiceId = typeServiceId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getBeneficaireId() {
        return beneficaireId;
    }

    public void setBeneficaireId(Long beneficiaireId) {
        this.beneficaireId = beneficiaireId;
    }

    public Long getEmetteurId() {
        return emetteurId;
    }

    public void setEmetteurId(Long emetteurId) {
        this.emetteurId = emetteurId;
    }

    public Long getPointVenteId() {
        return pointVenteId;
    }

    public void setPointVenteId(Long pointVenteId) {
        this.pointVenteId = pointVenteId;
    }

    public Long getPaysId() {
        return paysId;
    }

    public void setPaysId(Long paysId) {
        this.paysId = paysId;
    }

    public String getPaysLibellePays() {
        return paysLibellePays;
    }

    public void setPaysLibellePays(String paysLibellePays) {
        this.paysLibellePays = paysLibellePays;
    }

    public Long getCanalId() {
        return canalId;
    }

    public void setCanalId(Long canalId) {
        this.canalId = canalId;
    }

    public String getCanalLibelleCanal() {
        return canalLibelleCanal;
    }

    public void setCanalLibelleCanal(String canalLibelleCanal) {
        this.canalLibelleCanal = canalLibelleCanal;
    }

    public Long getUoAcquereurId() {
        return uoAcquereurId;
    }

    public void setUoAcquereurId(Long uniteOrganisationId) {
        this.uoAcquereurId = uniteOrganisationId;
    }

    public String getUoAcquereurLibelleUniteOrganisation() {
        return uoAcquereurLibelleUniteOrganisation;
    }

    public void setUoAcquereurLibelleUniteOrganisation(String uniteOrganisationLibelleUniteOrganisation) {
        this.uoAcquereurLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
    }

    public BigDecimal getMontantFraisEm() {
        return montantFraisEm;
    }

    public void setMontantFraisEm(BigDecimal montantFraisEm) {
        this.montantFraisEm = montantFraisEm;
    }

    public BigDecimal getMontantFraisAcq() {
        return montantFraisAcq;
    }

    public void setMontantFraisAcq(BigDecimal montantFraisAcq) {
        this.montantFraisAcq = montantFraisAcq;
    }

 

    public Long getMotifOperationId() {
        return motifOperationId;
    }

    public void setMotifOperationId(Long motifOperationId) {
        this.motifOperationId = motifOperationId;
    }

    public String getCompteDestValeurCompte() {
        return compteDestValeurCompte;
    }

    public void setCompteDestValeurCompte(String compteDestValeurCompte) {
        this.compteDestValeurCompte = compteDestValeurCompte;
    }

    public String getCompteSrcValeurCompte() {
        return compteSrcValeurCompte;
    }

    public void setCompteSrcValeurCompte(String compteSrcValeurCompte) {
        this.compteSrcValeurCompte = compteSrcValeurCompte;
    }

    public Long getCompteSrcId() {
        return compteSrcId;
    }

    public void setCompteSrcId(Long compteSrcId) {
        this.compteSrcId = compteSrcId;
    }

    public Long getCompteDestId() {
        return compteDestId;
    }

    public void setCompteDestId(Long compteDestId) {
        this.compteDestId = compteDestId;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getDateTraitement() {
        return dateTraitement;
    }

    public void setDateTraitement(String dateTraitement) {
        this.dateTraitement = dateTraitement;
    }

    public Long getOperationTraiteId() {
        return operationTraiteId;
    }

    public void setOperationTraiteId(Long operationTraiteId) {
        this.operationTraiteId = operationTraiteId;
    }

   

    public String getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(String dateDepart) {
        this.dateDepart = dateDepart;
    }

    public String getDateArrive() {
        return dateArrive;
    }

    public void setDateArrive(String dateArrive) {
        this.dateArrive = dateArrive;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Long getUoBeneficiaireId() {
        return uoBeneficiaireId;
    }

    public void setUoBeneficiaireId(Long uoBeneficiaireId) {
        this.uoBeneficiaireId = uoBeneficiaireId;
    }

    public String getUoBeneficiaireLibelleUniteOrganisation() {
        return uoBeneficiaireLibelleUniteOrganisation;
    }

    public void setUoBeneficiaireLibelleUniteOrganisation(String uoBeneficiaireLibelleUniteOrganisation) {
        this.uoBeneficiaireLibelleUniteOrganisation = uoBeneficiaireLibelleUniteOrganisation;
    }

    public String getBeneficiaireNum() {
        return beneficiaireNum;
    }

    public void setBeneficiaireNum(String beneficiaireNum) {
        this.beneficiaireNum = beneficiaireNum;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public BigDecimal getMontantFraisClient() {
        return montantFraisClient;
    }

    public void setMontantFraisClient(BigDecimal montantFraisClient) {
        this.montantFraisClient = montantFraisClient;
    }

  

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "MOperationDTO{" + "id=" + id + ", montantOp=" + montantOp + ", montantFrais=" +
                montantFrais + ", montantFraisClient=" + montantFraisClient + ", montantFraisEm="
                + montantFraisEm + ", montantFraisAcq=" + montantFraisAcq + ", montantTaxe=" + montantTaxe
                + ", dateOperation=" + dateOperation + ", statut=" + statut + ", codeRetrait=" 
                + codeRetrait + ", secret=" + secret + ", codeOperation=" + codeOperation + ", dateExpiration=" 
                + dateExpiration + ", motif=" + motif + ", codeExterne=" + codeExterne + ", typeServiceId=" + typeServiceId 
                + ", code=" + code + ", beneficaireId=" + beneficaireId + ", emetteurId=" + emetteurId + ", pointVenteId=" 
                + pointVenteId + ", paysId=" + paysId + ", paysLibellePays=" + paysLibellePays + ", canalId=" + canalId + ", canalLibelleCanal=" 
                + canalLibelleCanal + ", uoAcquereurId=" + uoAcquereurId + ", lieuPaiementId=" + lieuPaiementId + ", uoEmetteurId=" + uoEmetteurId 
                + ", uoBeneficiaireId=" + uoBeneficiaireId + ", montantOpEnDevise=" + montantOpEnDevise + ", devisePaiementId=" + devisePaiementId 
                + ", devisePaiementLibelle=" + devisePaiementLibelle + ", fseSrc=" + fseSrc + ", fseDest=" + fseDest + ", uoAcquereurLibelleUniteOrganisation=" 
                + uoAcquereurLibelleUniteOrganisation + ", uoBeneficiaireLibelleUniteOrganisation=" + uoBeneficiaireLibelleUniteOrganisation + ", uoEmetteurLibelleUniteOrganisation=" + uoEmetteurLibelleUniteOrganisation + ", dateDepart=" + dateDepart + ", dateArrive=" + dateArrive + ", pieceBeneficiaireId=" + pieceBeneficiaireId + ", pieceEmetteurId=" + pieceEmetteurId + ", compteDestValeurCompte=" + compteDestValeurCompte + ", compteSrcValeurCompte=" + compteSrcValeurCompte + ", compteSrcId=" + compteSrcId + ", compteDestId=" + compteDestId + ", commentaire=" + commentaire + ", dateTraitement=" + dateTraitement + ", userId=" + userId + ", login=" + login + ", telephone=" + telephone + ", beneficiaireNum=" + beneficiaireNum + ", operationTraiteId=" + operationTraiteId + ", total=" + total + ", motifOperationId=" + motifOperationId + ", "
               ;
    }

   

    
}
