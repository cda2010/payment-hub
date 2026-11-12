package com.gateway.payment.common.domain;

public enum StepType {
    START, FORM, API_CALL, OTP, ENTER_OTP, REDIRECT, WAIT_CALLBACK, CHECK_STATUS, SUCCESS, FAILURE
}
