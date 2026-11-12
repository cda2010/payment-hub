package com.m2i.omnicanal.presentation.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Réponse de /api/omnicanalPresentation.
 * codeErr, msg et text restent le contrat lu par les appelants existants.
 */
public class ReponseOperation {

    public static final String SUCCES = "SUCCES";
    public static final String ECHEC = "ECHEC";
    public static final String PARTIEL = "PARTIEL";

    private String codeErr;
    private String msg;
    private String text;
    private String statut;
    private OperationResume operation;
    private List<LigneReponse> lignes = new ArrayList<>();

    public String getCodeErr() {
        return codeErr;
    }

    public void setCodeErr(String codeErr) {
        this.codeErr = codeErr;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public OperationResume getOperation() {
        return operation;
    }

    public void setOperation(OperationResume operation) {
        this.operation = operation;
    }

    public List<LigneReponse> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneReponse> lignes) {
        this.lignes = lignes;
    }

    public static class OperationResume {
        private String code;
        private String date;
        private String referenceExterne;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getReferenceExterne() {
            return referenceExterne;
        }

        public void setReferenceExterne(String referenceExterne) {
            this.referenceExterne = referenceExterne;
        }
    }

    public static class LigneReponse {
        private int ordre;
        private boolean locale;
        private String codeErr;
        private String msg;

        public int getOrdre() {
            return ordre;
        }

        public void setOrdre(int ordre) {
            this.ordre = ordre;
        }

        public boolean isLocale() {
            return locale;
        }

        public void setLocale(boolean locale) {
            this.locale = locale;
        }

        public String getCodeErr() {
            return codeErr;
        }

        public void setCodeErr(String codeErr) {
            this.codeErr = codeErr;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
