package com.gateway.payment.omnicanal.validation;

public class ChannelValidationException extends RuntimeException {
    private final String code;

    public ChannelValidationException(String code, String message) {
        super(message);
        this.code = code != null ? code : "400";
    }

    public String getCode() { return code; }
}
