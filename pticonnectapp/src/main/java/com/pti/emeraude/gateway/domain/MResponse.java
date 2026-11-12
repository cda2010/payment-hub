package com.pti.emeraude.gateway.domain;

import java.util.List;

public class MResponse extends StandardResponse {

    private String code;
    private String message;
    private String tansactionId;
    private List<MParametre> extraData;
    private CustomerInformation customerInformation;
    private ExternalResponse externalResponse;
    private MClient mClient;

    public MResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public MResponse(String code, String message, String tansactionId, List<MParametre> extraData) {
        this.code = code;
        this.message = message;
        this.tansactionId = tansactionId;
        this.extraData = extraData;
    }

    public List<MParametre> getExtraData() {
        return extraData;
    }

    public void setExtraData(List<MParametre> extraData) {
        this.extraData = extraData;
    }

    public MResponse(String code, String message, String tansactionId) {
        this.code = code;
        this.message = message;
        this.tansactionId = tansactionId;
    }

    public MResponse() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public MResponse code(String code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MResponse message(String message) {
        this.message = message;
        return this;
    }

    public String getTansactionId() {
        return tansactionId;
    }

    public void setTansactionId(String tansactionId) {
        this.tansactionId = tansactionId;
    }

    public CustomerInformation getCustomerInformation() {
        return customerInformation;
    }

    public void setCustomerInformation(CustomerInformation customerInformation) {
        this.customerInformation = customerInformation;
    }

    public ExternalResponse getExternalResponse() {
        return externalResponse;
    }

    public void setExternalResponse(ExternalResponse externalResponse) {
        this.externalResponse = externalResponse;
    }

    public MClient getmClient() {
        return mClient;
    }

    public void setmClient(MClient mClient) {
        this.mClient = mClient;
    }
}
