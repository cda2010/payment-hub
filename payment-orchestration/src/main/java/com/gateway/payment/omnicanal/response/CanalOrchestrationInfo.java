package com.gateway.payment.omnicanal.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CanalOrchestrationInfo {
    private String engine;
    private String workflow;
    private String patternCode;
    private String schemaId;
    private String stepSpec;
    private String errorCode;
    private String errorMessage;
    private String failedStepKey;

    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }
    public String getWorkflow() { return workflow; }
    public void setWorkflow(String workflow) { this.workflow = workflow; }
    public String getPatternCode() { return patternCode; }
    public void setPatternCode(String patternCode) { this.patternCode = patternCode; }
    public String getSchemaId() { return schemaId; }
    public void setSchemaId(String schemaId) { this.schemaId = schemaId; }
    public String getStepSpec() { return stepSpec; }
    public void setStepSpec(String stepSpec) { this.stepSpec = stepSpec; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getFailedStepKey() { return failedStepKey; }
    public void setFailedStepKey(String failedStepKey) { this.failedStepKey = failedStepKey; }
}
