package com.m2i.pti.web.rest.vm;

import jakarta.validation.constraints.Size;

public class PasswordVM {

    private static final int PASSWORD_MIN_LENGTH = 8;

    private static final int PASSWORD_MAX_LENGTH = 50;

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String newPassword;

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String oldPassword;

    public PasswordVM() {
    }

    public PasswordVM(String newPassword, String oldPassword) {
        this.newPassword = newPassword;
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
}
