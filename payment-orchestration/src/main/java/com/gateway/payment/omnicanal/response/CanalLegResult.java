package com.gateway.payment.omnicanal.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CanalLegResult {
    private String stepId;
    private String typeMethode;
    private String codeFse;
    private String providerCode;
    private boolean success;
    private String providerStatus;
    private String providerMessage;
    private String providerReference;

    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }
    public String getTypeMethode() { return typeMethode; }
    public void setTypeMethode(String typeMethode) { this.typeMethode = typeMethode; }
    public String getCodeFse() { return codeFse; }
    public void setCodeFse(String codeFse) { this.codeFse = codeFse; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getProviderStatus() { return providerStatus; }
    public void setProviderStatus(String providerStatus) { this.providerStatus = providerStatus; }
    public String getProviderMessage() { return providerMessage; }
    public void setProviderMessage(String providerMessage) { this.providerMessage = providerMessage; }
    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }
}
