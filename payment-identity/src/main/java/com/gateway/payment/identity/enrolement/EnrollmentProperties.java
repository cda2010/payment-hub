package com.gateway.payment.identity.enrolement;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** En local, le code est renvoyé dans la réponse pour pouvoir terminer le parcours. */
@Component
@ConfigurationProperties(prefix = "enrollment")
public class EnrollmentProperties {

    private boolean exposeCode;

    private String documentsUrl = "http://127.0.0.1:8101/api/v1/documents";

    private String catalogDbUrl = "jdbc:postgresql://localhost:5432/payment_gateway";

    private String catalogDbUser = "gateway";

    private String catalogDbPassword = "gateway";

    private String formsDir = "payment-channel-api/src/main/resources/modules/payment-admin-module/forms";

    private String piecesDmn = "payment-channel-api/src/main/resources/modules/payment-admin-module/decisions/enrolement-pieces.dmn";

    private String profilLimiteDmn = "payment-channel-api/src/main/resources/modules/payment-admin-module/decisions/enrolement-profil-limite.dmn";

    private String networkDbUrl = "jdbc:postgresql://localhost:5438/payment_network";

    private String networkDbUser = "network";

    private String networkDbPassword = "network";

    /** Passerelle qui expose GETINFOS et GETINFOSCOMPTE. Vide : l'annuaire local répond. */
    private String omnicanalUrl = "http://127.0.0.1:8080";

    public boolean isExposeCode() {
        return exposeCode;
    }

    public void setExposeCode(boolean exposeCode) {
        this.exposeCode = exposeCode;
    }

    public String getDocumentsUrl() {
        return documentsUrl;
    }

    public void setDocumentsUrl(String documentsUrl) {
        this.documentsUrl = documentsUrl;
    }

    public String getCatalogDbUrl() {
        return catalogDbUrl;
    }

    public void setCatalogDbUrl(String catalogDbUrl) {
        this.catalogDbUrl = catalogDbUrl;
    }

    public String getCatalogDbUser() {
        return catalogDbUser;
    }

    public void setCatalogDbUser(String catalogDbUser) {
        this.catalogDbUser = catalogDbUser;
    }

    public String getCatalogDbPassword() {
        return catalogDbPassword;
    }

    public void setCatalogDbPassword(String catalogDbPassword) {
        this.catalogDbPassword = catalogDbPassword;
    }

    public String getFormsDir() {
        return formsDir;
    }

    public void setFormsDir(String formsDir) {
        this.formsDir = formsDir;
    }

    public String getPiecesDmn() {
        return piecesDmn;
    }

    public void setPiecesDmn(String piecesDmn) {
        this.piecesDmn = piecesDmn;
    }

    public String getProfilLimiteDmn() {
        return profilLimiteDmn;
    }

    public void setProfilLimiteDmn(String profilLimiteDmn) {
        this.profilLimiteDmn = profilLimiteDmn;
    }

    public String getNetworkDbUrl() {
        return networkDbUrl;
    }

    public void setNetworkDbUrl(String networkDbUrl) {
        this.networkDbUrl = networkDbUrl;
    }

    public String getNetworkDbUser() {
        return networkDbUser;
    }

    public void setNetworkDbUser(String networkDbUser) {
        this.networkDbUser = networkDbUser;
    }

    public String getNetworkDbPassword() {
        return networkDbPassword;
    }

    public void setNetworkDbPassword(String networkDbPassword) {
        this.networkDbPassword = networkDbPassword;
    }

    public String getOmnicanalUrl() {
        return omnicanalUrl;
    }

    public void setOmnicanalUrl(String omnicanalUrl) {
        this.omnicanalUrl = omnicanalUrl;
    }
}
