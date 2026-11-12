package com.gateway.payment.omnicanal.response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpecRunResult {
    private boolean error;
    private OrchErrorCode errorCode;
    private String message;
    private UUID schemaId;
    private String workflowName;
    private String patternCode;
    private List<CanalLegResult> legs = new ArrayList<>();
    private boolean compensated;

    public static SpecRunResult ok(UUID schemaId, String workflow, String pattern) {
        SpecRunResult r = new SpecRunResult();
        r.error = false;
        r.errorCode = OrchErrorCode.ORCH_OK;
        r.schemaId = schemaId;
        r.workflowName = workflow;
        r.patternCode = pattern;
        r.message = "OK";
        return r;
    }

    public static SpecRunResult fail(UUID schemaId, OrchErrorCode code, String message) {
        SpecRunResult r = new SpecRunResult();
        r.error = true;
        r.errorCode = code != null ? code : OrchErrorCode.ORCH_INTERNAL;
        r.schemaId = schemaId;
        r.message = message;
        return r;
    }

    public boolean error() { return error; }
    public void setError(boolean error) { this.error = error; }
    public OrchErrorCode getErrorCode() { return errorCode; }
    public void setErrorCode(OrchErrorCode errorCode) { this.errorCode = errorCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public UUID getSchemaId() { return schemaId; }
    public void setSchemaId(UUID schemaId) { this.schemaId = schemaId; }
    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
    public String getPatternCode() { return patternCode; }
    public void setPatternCode(String patternCode) { this.patternCode = patternCode; }
    public List<CanalLegResult> getLegs() { return legs; }
    public void setLegs(List<CanalLegResult> legs) { this.legs = legs; }
    public boolean isCompensated() { return compensated; }
    public void setCompensated(boolean compensated) { this.compensated = compensated; }
}
