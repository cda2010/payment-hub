package com.gateway.payment.omnicanal.policy;

import org.springframework.util.StringUtils;

public final class ErrorClassifier {

    private ErrorClassifier() {}

    public static ErrorType classifyType(String orchErrorCode, Integer causeErrCode, String message) {
        String msg = message != null ? message.toLowerCase() : "";
        String code = orchErrorCode != null ? orchErrorCode.toUpperCase() : "";
        if (code.contains("TIMEOUT") || msg.contains("timeout") || msg.contains("timed out")) {
            return ErrorType.TIMEOUT;
        }
        if (code.contains("TRANSIENT") || code.contains("UNAVAILABLE") || msg.contains("503")
                || msg.contains("connection") || msg.contains("i/o error")
                || msg.contains("connection refused") || msg.contains("oauth")
                || msg.contains("indisponib") || msg.contains("token")) {
            return ErrorType.TRANSIENT;
        }
        if (causeErrCode != null && causeErrCode != 0) {
            return ErrorType.BUSINESS_REJECTED;
        }
        if (code.contains("BUSINESS") || code.contains("REJECT")) {
            return ErrorType.BUSINESS_REJECTED;
        }
        if (StringUtils.hasText(code) || StringUtils.hasText(message)) {
            return ErrorType.TECHNICAL;
        }
        return ErrorType.UNKNOWN;
    }
}
