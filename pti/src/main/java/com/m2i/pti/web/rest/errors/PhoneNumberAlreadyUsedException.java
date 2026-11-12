package com.m2i.pti.web.rest.errors;

public class PhoneNumberAlreadyUsedException extends BadRequestAlertException {

    public PhoneNumberAlreadyUsedException() {
        super(ErrorConstants.PHONE_NUMBER_ALREADY_USED_TYPE, "Phone Number already in use", "userManagement", "phoneNumberexists");
    }
}
