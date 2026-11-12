package com.gateway.payment.orchestration.dmn;

/**
 * Constantes du DMN de matching schéma — aligné Omnicanal {@code schema-matching.dmn}.
 * <ul>
 *   <li>Déploiement / catalogue RapidFrame : {@code modules/payment-admin-module/decisions/}</li>
 *   <li>Copie classpath tests / stub : {@code rules/schema-matching.dmn}</li>
 * </ul>
 * Runtime : Flowable DMN (RapidFrame), même table de décision que KIE côté Omnicanal.
 */
public final class DmnSchemaMatchingConfig {

    public static final String DMN_RESOURCE = "rules/schema-matching.dmn";
    public static final String DMN_MODULE_RESOURCE =
            "modules/payment-admin-module/decisions/schema-matching.dmn";
    public static final String DMN_NAMESPACE = "https://omnicanal.m2i.com/dmn/schema-matching";
    public static final String DMN_MODEL_NAME = "schema-matching";
    /** Nom de décision OMG (Omnicanal / KIE). */
    public static final String DECISION_NAME = "SchemaMatch";
    /** Clé Flowable = {@code id} de la décision dans le DMN. */
    public static final String DECISION_KEY = "decision_SchemaMatch";
    public static final String OUTPUT_MATCHED = "matched";

    private DmnSchemaMatchingConfig() {
    }
}
