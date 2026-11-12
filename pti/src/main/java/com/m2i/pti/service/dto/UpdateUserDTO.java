package com.m2i.pti.service.dto;

public class UpdateUserDTO {

    private Long Id;
    private String login;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String codeIndicatif;
    private String codeActeur;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCodeIndicatif() {
        return codeIndicatif;
    }

    public void setCodeIndicatif(String codeIndicatif) {
        this.codeIndicatif = codeIndicatif;
    }

    public String getCodeActeur() {
        return codeActeur;
    }

    public void setCodeActeur(String codeActeur) {
        this.codeActeur = codeActeur;
    }
}
