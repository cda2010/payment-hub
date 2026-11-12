package com.gateway.payment.common.i18n;

/**
 * Portées et chemins de champs utilisés dans {@code i18n_translation}.
 * Les chemins doivent rester alignés avec la reprise SQL (V84).
 */
public final class I18nScope {

    public static final String CATALOG_DE_SERVICE = "CATALOG_DE_SERVICE";
    public static final String CATEGORIE_SERVICE = "CATEGORIE_SERVICE";
    public static final String CATALOG_DE_SERVICE_VERSION = "CATALOG_DE_SERVICE_VERSION";

    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";

    private I18nScope() {}

    /** {@code configuration.roles[SOURCE].label} */
    public static String rolePath(String roleCode) {
        return "configuration.roles[" + roleCode + "].label";
    }

    /** {@code configuration.recap.title} */
    public static String recapPath(String key) {
        return "configuration.recap." + key;
    }

    /** {@code configuration.recap.fields[amount].label} */
    public static String recapFieldPath(String fieldKey) {
        return "configuration.recap.fields[" + fieldKey + "].label";
    }

    /** {@code configuration.presentation.sections[favorites].title} */
    public static String sectionTitlePath(String sectionId) {
        return "configuration.presentation.sections[" + sectionId + "].title";
    }

    /** {@code configuration.history.titleTemplate} / {@code kindLabel} / {@code subtitleTemplate} */
    public static String historyPath(String key) {
        return "configuration.history." + key;
    }

    /** {@code configuration.history.detailFields[agentDisplay].label} */
    public static String historyDetailFieldPath(String fieldKey) {
        return "configuration.history.detailFields[" + fieldKey + "].label";
    }

    /** {@code configuration.notifications[SUCCESS].body} */
    public static String notificationPath(String eventCode, String field) {
        return "configuration.notifications[" + eventCode + "]." + field;
    }
}
