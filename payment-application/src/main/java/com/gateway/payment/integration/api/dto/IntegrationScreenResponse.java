package com.gateway.payment.integration.api.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Écran produit pour intégrateurs externes (équivalent RapidFrame {@code ApiScreenResponse}).
 * Produit par {@code /api/v1/gateway/*} à partir de {@link com.gateway.payment.payment.api.dto.PaymentResponse}.
 */
public class IntegrationScreenResponse {

    private String type = "FORM";
    private String title;
    private String message;
    private String formKey;
    private Map<String, Object> formSchema;
    private List<Map<String, Object>> fields;
    private Map<String, Object> data = new LinkedHashMap<>();
    private List<Map<String, Object>> actions;
    private Integer otpLength;
    private String url;
    private Integer pollAfterSeconds;
    private Map<String, Object> meta = new LinkedHashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    public Map<String, Object> getFormSchema() {
        return formSchema;
    }

    public void setFormSchema(Map<String, Object> formSchema) {
        this.formSchema = formSchema;
    }

    public List<Map<String, Object>> getFields() {
        return fields;
    }

    public void setFields(List<Map<String, Object>> fields) {
        this.fields = fields;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data != null ? data : new LinkedHashMap<>();
    }

    public List<Map<String, Object>> getActions() {
        return actions;
    }

    public void setActions(List<Map<String, Object>> actions) {
        this.actions = actions;
    }

    public Integer getOtpLength() {
        return otpLength;
    }

    public void setOtpLength(Integer otpLength) {
        this.otpLength = otpLength;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPollAfterSeconds() {
        return pollAfterSeconds;
    }

    public void setPollAfterSeconds(Integer pollAfterSeconds) {
        this.pollAfterSeconds = pollAfterSeconds;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta != null ? meta : new LinkedHashMap<>();
    }
}
