package com.m2i.omnicanal.presentation.execution;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "ordre_partenaire")
public class OrdrePartenaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cle_idempotence", nullable = false, unique = true, length = 200)
    private String cleIdempotence;

    @Column(name = "code_fse", length = 40)
    private String codeFse;

    @Column(name = "code_api", length = 40)
    private String codeApi;

    @Column(name = "code_methode", length = 80)
    private String codeMethode;

    @Column(name = "mode_execution", length = 30)
    private String modeExecution;

    @Column(nullable = false, length = 30)
    private String statut;

    @Column(name = "err_code")
    private Integer errCode;

    @Column(length = 500)
    private String message;

    @Column(columnDefinition = "text")
    private String reponse;

    @Column(name = "callback_url", length = 500)
    private String callbackUrl;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "mis_a_jour_le", nullable = false)
    private Instant misAJourLe;

    public Long getId() {
        return id;
    }

    public String getCleIdempotence() {
        return cleIdempotence;
    }

    public void setCleIdempotence(String cleIdempotence) {
        this.cleIdempotence = cleIdempotence;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public String getCodeApi() {
        return codeApi;
    }

    public void setCodeApi(String codeApi) {
        this.codeApi = codeApi;
    }

    public String getCodeMethode() {
        return codeMethode;
    }

    public void setCodeMethode(String codeMethode) {
        this.codeMethode = codeMethode;
    }

    public String getModeExecution() {
        return modeExecution;
    }

    public void setModeExecution(String modeExecution) {
        this.modeExecution = modeExecution;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getErrCode() {
        return errCode;
    }

    public void setErrCode(Integer errCode) {
        this.errCode = errCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(Instant creeLe) {
        this.creeLe = creeLe;
    }

    public Instant getMisAJourLe() {
        return misAJourLe;
    }

    public void setMisAJourLe(Instant misAJourLe) {
        this.misAJourLe = misAJourLe;
    }
}
