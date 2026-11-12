package com.gateway.payment.bpm;

/**
 * Constantes BPMN orchestration schéma (LigneSchemaEcrit).
 */
public final class GatewayCallFseBpmnErrors {

    public static final String STEP_FAILED = "CALL_FSE_STEP_FAILED";
    public static final String VAR_SCHEMA_ECRITURE_ID = "schemaEcritureId";
    public static final String VAR_SOURCE_CODE_FSE = "sourceCodeFse";
    public static final String VAR_DEST_CODE_FSE = "destCodeFse";
    public static final String VAR_USER_PARAMS = "userParams";

    private GatewayCallFseBpmnErrors() {
    }
}
