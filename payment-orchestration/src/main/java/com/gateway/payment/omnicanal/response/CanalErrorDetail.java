package com.gateway.payment.omnicanal.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CanalErrorDetail {
    private String code;
    private String message;
    private String stepKey;

    public CanalErrorDetail() {}
    public CanalErrorDetail(String code, String message, String stepKey) {
        this.code = code;
        this.message = message;
        this.stepKey = stepKey;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStepKey() { return stepKey; }
    public void setStepKey(String stepKey) { this.stepKey = stepKey; }
}
