package com.gateway.payment.bpm.dmn;

/**
 * DMN montant / provider → {@code documentProfileCode}.
 */
public final class DmnDocumentRequirementsConfig {

    public static final String DMN_RESOURCE = "rules/document-requirements.dmn";
    public static final String DMN_MODULE_RESOURCE =
            "modules/payment-admin-module/decisions/document-requirements.dmn";
    public static final String DECISION_NAME = "DocumentRequirements";
    public static final String DECISION_KEY = "decision_DocumentRequirements";

    public static final String OUT_PROFILE_CODE = "documentProfileCode";
    public static final String VAR_PROFILE_CODE = "documentProfileCode";
    public static final String VAR_DOCUMENTS_REQUIRED = "documentsRequired";
    public static final String VAR_REQUIRED_DOCS = "requiredDocs";
    public static final String VAR_OPTIONAL_DOCS = "optionalDocs";
    public static final String VAR_DOC_APPROVAL = "documentsRequireApproval";
    public static final String CTX_DOCUMENTS = "documents";

    public static final String PROFILE_NONE = "NONE";

    private DmnDocumentRequirementsConfig() {}
}
