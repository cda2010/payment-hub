package com.gateway.payment.orchestration.domain;

/** Codes pattern matching DMN (aligné Omnicanal SchemaMatchPatterns). */
public final class SchemaPatternCode {

    public static final String SAME_FSE_ANY = "SAME_FSE_ANY";
    public static final String SAME_FSE_SIEGE_1 = "SAME_FSE_SIEGE_1";
    public static final String SAME_FSE_SIEGE_0 = "SAME_FSE_SIEGE_0";
    public static final String SAME_FSE_SIEGE_TRUTHY = "SAME_FSE_SIEGE_TRUTHY";
    public static final String DIFF_FSE_SIEGE_1 = "DIFF_FSE_SIEGE_1";
    public static final String DIFF_FSE_SIEGE_0 = "DIFF_FSE_SIEGE_0";
    public static final String DIFF_FSE_ANY = "DIFF_FSE_ANY";
    public static final String INTERNATIONAL = "INTERNATIONAL";
    public static final String BANQUE_BANQUE = "BANQUE_BANQUE";
    public static final String NON_BANQUE_SAME_PAYS = "NON_BANQUE_SAME_PAYS";
    public static final String DEST_FSE_0006 = "DEST_FSE_0006";
    public static final String SOURCE_FSE_0007 = "SOURCE_FSE_0007";

    private SchemaPatternCode() {
    }

    public static java.util.List<String> all() {
        return java.util.List.of(
                SAME_FSE_ANY, SAME_FSE_SIEGE_1, SAME_FSE_SIEGE_0, SAME_FSE_SIEGE_TRUTHY,
                DIFF_FSE_SIEGE_1, DIFF_FSE_SIEGE_0, DIFF_FSE_ANY,
                INTERNATIONAL, BANQUE_BANQUE, NON_BANQUE_SAME_PAYS,
                DEST_FSE_0006, SOURCE_FSE_0007
        );
    }
}
