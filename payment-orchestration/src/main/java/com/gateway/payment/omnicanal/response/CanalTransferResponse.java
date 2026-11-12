package com.gateway.payment.omnicanal.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/** Réponse canal enrichie — compatible Message Omnicanal (codeErr/msg/text). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CanalTransferResponse {
    private String codeErr;
    private String msg;
    private String text;
    private Object responseEntity;
    private String correlationId;
    private String paymentId;
    private String codeOperation;
    private CanalOrchestrationInfo orchestration;
    private List<CanalLegResult> legs = new ArrayList<>();
    private CanalErrorDetail error;

    public static CanalTransferResponse ok(String msg) {
        CanalTransferResponse r = new CanalTransferResponse();
        r.codeErr = "0";
        r.msg = msg != null ? msg : "OK";
        return r;
    }

    public static CanalTransferResponse fail(String code, String msg) {
        CanalTransferResponse r = new CanalTransferResponse();
        r.codeErr = code != null ? code : "1";
        r.msg = msg;
        r.error = new CanalErrorDetail(code, msg, null);
        return r;
    }

    public String getCodeErr() { return codeErr; }
    public void setCodeErr(String codeErr) { this.codeErr = codeErr; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Object getResponseEntity() { return responseEntity; }
    public void setResponseEntity(Object responseEntity) { this.responseEntity = responseEntity; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getCodeOperation() { return codeOperation; }
    public void setCodeOperation(String codeOperation) { this.codeOperation = codeOperation; }
    public CanalOrchestrationInfo getOrchestration() { return orchestration; }
    public void setOrchestration(CanalOrchestrationInfo orchestration) { this.orchestration = orchestration; }
    public List<CanalLegResult> getLegs() { return legs; }
    public void setLegs(List<CanalLegResult> legs) { this.legs = legs != null ? legs : new ArrayList<>(); }
    public CanalErrorDetail getError() { return error; }
    public void setError(CanalErrorDetail error) { this.error = error; }
}
