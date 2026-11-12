package com.m2i.pti.service.dto;

public class UserToUpdate {
    public UpdateUserDTO oldUser;
    public UpdateUserDTO newUser;

    public UpdateUserDTO getOldUser() {
        return oldUser;
    }

    public void setOldUser(UpdateUserDTO oldUser) {
        this.oldUser = oldUser;
    }

    public UpdateUserDTO getNewUser() {
        return newUser;
    }

    public void setNewUser(UpdateUserDTO newUser) {
        this.newUser = newUser;
    }

}
