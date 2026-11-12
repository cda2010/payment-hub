package com.m2i.pti.service.dto;





import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.Etape;
import com.m2i.pti.domain.enumeration.StatutOperation;

import jakarta.persistence.Lob;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the Operation entity.
 */
public class OperationDTO implements Serializable {

    private Long id;

    private BigDecimal montantOp;

    private BigDecimal montantFrais;
    private BigDecimal montantFraisClient;

    private BigDecimal montantFraisEm;

    private BigDecimal montantFraisAcq;

    private BigDecimal montantTaxe;
   

    private ZonedDateTime dateOperation;

    private StatutOperation statut;

    private Etape etape;

    private String codeRetrait;

    private String secret;

    private String codeOperation;

    private LocalDate dateExpiration;

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

    private UniteOrganisation fseSrc;

    private UniteOrganisation fseDest;

    private String uoAcquereurLibelleUniteOrganisation;
    private String uoBeneficiaireLibelleUniteOrganisation;
    private String uoEmetteurLibelleUniteOrganisation;

    private String canalRetrait;

    private String numVersement;

    private String dateDepart;

    private String dateArrive;

    private Long pieceBeneficiaireId;

    private Long pieceEmetteurId;

    @Lob
    private byte[] bordereau;
    private String bordereauContentType;

    private String compteDestValeurCompte;

    private String compteSrcValeurCompte;

    private Long compteSrcId;

    private Long compteDestId;

    private String commentaire;


    private ZonedDateTime dateTraitement;

    private Long userId;

    private String login;

    private String telephone;

    private String beneficiaireNum;

    private Long operationTraiteId;

    private BigDecimal total;

    private Long motifOperationId;
   
    private Instant createdDate;
    private Instant lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
  private String description;
    private String error;
    public OperationDTO() {
    }

    public OperationDTO(Long id) {
        this.id = id;
    }

    public OperationDTO(Long id, BigDecimal montantOp, BigDecimal montantFrais, BigDecimal montantFraisClient, BigDecimal montantFraisEm, BigDecimal montantFraisAcq, BigDecimal montantTaxe) {
        this.id = id;
        this.montantOp = montantOp;
        this.montantFrais = montantFrais;
        this.montantFraisClient = montantFraisClient;
        this.montantFraisEm = montantFraisEm;
        this.montantFraisAcq = montantFraisAcq;
        this.montantTaxe = montantTaxe;
    }

    public OperationDTO(Long id, BigDecimal montantOp, BigDecimal montantFrais, BigDecimal montantFraisClient, BigDecimal montantFraisEm, BigDecimal montantFraisAcq, BigDecimal montantTaxe, ZonedDateTime dateOperation, String statut, String etape, String codeRetrait, String secret, String codeOperation, LocalDate dateExpiration, String motif, String codeExterne, Long typeServiceId, String code, Long emetteurId, Long pointVenteId, Long paysId, Long canalId, Long uoAcquereurId, Long uoEmetteurId, Long uoBeneficiaireId, BigDecimal montantOpEnDevise, Long devisePaiementId, Long compteSrcId, Long compteDestId, String commentaire, Long userId,Instant createdDate, String createdBy,String lastModifiedBy,Instant lastModifiedDate,Long motifOperationId,Long beneficaireId, String description) {
        this.id = id;
        this.montantOp = montantOp;
        this.montantFrais = montantFrais;
        this.montantFraisClient = montantFraisClient;
        this.montantFraisEm = montantFraisEm;
        this.montantFraisAcq = montantFraisAcq;
        this.montantTaxe = montantTaxe;
        this.dateOperation = dateOperation;
        this.statut = statut!=null?StatutOperation.valueOf(statut):null;
        this.etape = etape!=null?Etape.valueOf(etape):null;
        this.codeRetrait = codeRetrait;
        this.secret = secret;
        this.codeOperation = codeOperation;
        this.dateExpiration = dateExpiration;
        this.motif = motif;
        this.codeExterne = codeExterne;
        this.typeServiceId = typeServiceId;
        this.code = code;
        this.emetteurId = emetteurId;
        this.pointVenteId = pointVenteId;
        this.paysId = paysId;
        this.canalId = canalId;
        this.uoAcquereurId = uoAcquereurId;
        this.uoEmetteurId = uoEmetteurId;
        this.uoBeneficiaireId = uoBeneficiaireId;
        this.montantOpEnDevise = montantOpEnDevise;
        this.devisePaiementId = devisePaiementId;
        this.compteSrcId = compteSrcId;
        this.compteDestId = compteDestId;
        this.commentaire = commentaire;
        this.userId = userId;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
        this.motifOperationId = motifOperationId;
        this.beneficaireId = beneficaireId;
        this.description=description;
    }

    public OperationDTO(Long id, BigDecimal montantOp, BigDecimal montantFrais, BigDecimal montantFraisClient,
                        BigDecimal montantFraisEm, BigDecimal montantFraisAcq, BigDecimal montantTaxe,
                        ZonedDateTime dateOperation, String statut, String etape, String codeRetrait,
                        String secret, String codeOperation, LocalDate dateExpiration, String motif,
                        String codeExterne, Long typeServiceId, Long emetteurId, Long pointVenteId,
                        Long paysId, Long canalId, Long uoAcquereurId, Long uoEmetteurId, Long uoBeneficiaireId,
                        BigDecimal montantOpEnDevise, Long devisePaiementId, Long compteSrcId, Long compteDestId,
                        String commentaire, Long userId,Instant createdDate, String createdBy,Long motifOperationId,
                        Long beneficaireId,Long pieceEmetteurId,Long pieceBeneficiaireId,String description) {
        this.id = id;
        this.montantOp = montantOp;
        this.montantFrais = montantFrais;
        this.montantFraisClient = montantFraisClient;
        this.montantFraisEm = montantFraisEm;
        this.montantFraisAcq = montantFraisAcq;
        this.montantTaxe = montantTaxe;
        this.dateOperation = dateOperation;
        this.statut = statut!=null?StatutOperation.valueOf(statut):null;
        this.etape = etape!=null?Etape.valueOf(etape):null;
        this.codeRetrait = codeRetrait;
        this.secret = secret;
        this.codeOperation = codeOperation;
        this.dateExpiration = dateExpiration;
        this.motif = motif;
        this.codeExterne = codeExterne;
        this.typeServiceId = typeServiceId;
        this.emetteurId = emetteurId;
        this.pointVenteId = pointVenteId;
        this.paysId = paysId;
        this.canalId = canalId;
        this.uoAcquereurId = uoAcquereurId;
        this.uoEmetteurId = uoEmetteurId;
        this.uoBeneficiaireId = uoBeneficiaireId;
        this.montantOpEnDevise = montantOpEnDevise;
        this.devisePaiementId = devisePaiementId;
        this.compteSrcId = compteSrcId;
        this.compteDestId = compteDestId;
        this.commentaire = commentaire;
        this.userId = userId;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
        this.motifOperationId = motifOperationId;
        this.beneficaireId = beneficaireId;
        this.pieceEmetteurId = pieceEmetteurId;
        this.pieceBeneficiaireId = pieceBeneficiaireId;
        this.description=description;
    }
    public OperationDTO(Long id, BigDecimal montantOp, ZonedDateTime dateOperation, Long pointVenteId){
        this.id = id;
        this.montantOp = montantOp;
        this.dateOperation = dateOperation;
        this.pointVenteId = pointVenteId;
    }





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

    public UniteOrganisation getFseSrc() {
        return fseSrc;
    }

    public void setFseSrc(UniteOrganisation fseSrc) {
        this.fseSrc = fseSrc;
    }

    public UniteOrganisation getFseDest() {
        return fseDest;
    }

    public void setFseDest(UniteOrganisation fseDest) {
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

    private Set<TaxeOperationDTO> taxes = new HashSet<>();

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

    public Set<TaxeOperationDTO> getTaxes() {
        return taxes;
    }

    public void setTaxes(Set<TaxeOperationDTO> taxes) {
        this.taxes = taxes;
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

    public String getCanalRetrait() {
        return canalRetrait;
    }

    public void setCanalRetrait(String canalRetrait) {
        this.canalRetrait = canalRetrait;
    }

    public String getNumVersement() {
        return numVersement;
    }

    public void setNumVersement(String numVersement) {
        this.numVersement = numVersement;
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

    public ZonedDateTime getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(ZonedDateTime dateOperation) {
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

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
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

    public byte[] getBordereau() {
        return bordereau;
    }

    public void setBordereau(byte[] bordereau) {
        this.bordereau = bordereau;
    }

    public String getBordereauContentType() {
        return bordereauContentType;
    }

    public void setBordereauContentType(String bordereauContentType) {
        this.bordereauContentType = bordereauContentType;
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

    public ZonedDateTime getDateTraitement() {
        return dateTraitement;
    }

    public void setDateTraitement(ZonedDateTime dateTraitement) {
        this.dateTraitement = dateTraitement;
    }

    public Long getOperationTraiteId() {
        return operationTraiteId;
    }

    public void setOperationTraiteId(Long operationTraiteId) {
        this.operationTraiteId = operationTraiteId;
    }

    public Etape getEtape() {
        return etape;
    }

    public void setEtape(Etape etape) {
        this.etape = etape;
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

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OperationDTO operationDTO = (OperationDTO) o;
        if (operationDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), operationDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {

        return "OperationDTO{" +
                "id=" + getId() +
                ", montantOp=" + getMontantOp() +
                ", montantFrais=" + getMontantFrais() +
                ", dateOperation='" + getDateOperation() + "'" +
                ", statut='" + getStatut() + "'" +
               
                ", secret='" + getSecret() + "'" +
                ", codeOperation='" + getCodeOperation() + "'" +
                ", dateExpiration='" + getDateExpiration() + "'" +
                ", motif='" + getMotif() + "'" +
                ", codeExterne='" + getCodeExterne() + "'" +
                ", commentaire='" + getCommentaire() + "'" +

                "}";

    }
}
