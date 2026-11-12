package com.m2i.pti.mobile.dto;

import java.io.Serializable;

public class UserExist implements Serializable {

    private Boolean passwordReset = false;
    private Boolean disabled = false;
    private Boolean passwordMatch = false;
    private Boolean userRemoved = false;
    private Boolean updateAccount = false;
    private Boolean updateData = false;
    private String data = "";
    private Long id;

    public Boolean getPasswordReset() {
        return passwordReset;
    }

    public void setPasswordReset(Boolean passwordReset) {
        this.passwordReset = passwordReset;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getPasswordMatch() {
        return passwordMatch;
    }

    public void setPasswordMatch(Boolean passwordMatch) {
        this.passwordMatch = passwordMatch;
    }

    public Boolean getUserRemoved() {
        return userRemoved;
    }

    public void setUserRemoved(Boolean userRemoved) {
        this.userRemoved = userRemoved;
    }

    public Boolean getUpdateAccount() {
        return updateAccount;
    }

    public void setUpdateAccount(Boolean updateAccount) {
        this.updateAccount = updateAccount;
    }

    public Boolean getUpdateData() {
        return updateData;
    }

    public void setUpdateData(Boolean updateData) {
        this.updateData = updateData;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
