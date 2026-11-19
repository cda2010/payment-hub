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

    private String profilOffreDmn = "payment-channel-api/src/main/resources/modules/payment-admin-module/decisions/enrolement-profil-offre.dmn";

    private String networkDbUrl = "jdbc:postgresql://localhost:5438/payment_network";

    private String networkDbUser = "network";

    private String networkDbPassword = "network";

    /** Passerelle : le moteur boundaryByTypeMethode. Vide : l'annuaire local répond. */
    private String omnicanalUrl = "http://127.0.0.1:8080";

    /** Même clé que gateway.canal.api-key. Le moteur canal l'exige. */
    private String canalApiKey = "change-me-mobile";

    /** payment-alias : l'alias du compte reconnu y est créé. */
    private String aliasUrl = "http://127.0.0.1:8097/api/v1/aliases";

    private String keycloakUrl = "http://127.0.0.1:8180";

    private String keycloakRealm = "payment";

    private String keycloakAdmin = "admin";

    private String keycloakAdminPassword = "admin";

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

    public String getProfilOffreDmn() {
        return profilOffreDmn;
    }

    public void setProfilOffreDmn(String profilOffreDmn) {
        this.profilOffreDmn = profilOffreDmn;
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

    public String getCanalApiKey() {
        return canalApiKey;
    }

    public void setCanalApiKey(String canalApiKey) {
        this.canalApiKey = canalApiKey;
    }

    public String getAliasUrl() {
        return aliasUrl;
    }

    public void setAliasUrl(String aliasUrl) {
        this.aliasUrl = aliasUrl;
    }

    public String getKeycloakUrl() {
        return keycloakUrl;
    }

    public void setKeycloakUrl(String keycloakUrl) {
        this.keycloakUrl = keycloakUrl;
    }

    public String getKeycloakRealm() {
        return keycloakRealm;
    }

    public void setKeycloakRealm(String keycloakRealm) {
        this.keycloakRealm = keycloakRealm;
    }

    public String getKeycloakAdmin() {
        return keycloakAdmin;
    }

    public void setKeycloakAdmin(String keycloakAdmin) {
        this.keycloakAdmin = keycloakAdmin;
    }

    public String getKeycloakAdminPassword() {
        return keycloakAdminPassword;
    }

    public void setKeycloakAdminPassword(String keycloakAdminPassword) {
        this.keycloakAdminPassword = keycloakAdminPassword;
    }
}
