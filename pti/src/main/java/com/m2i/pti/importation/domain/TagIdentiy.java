package com.m2i.pti.importation.domain;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Trim;
/**
 * @author O.soufiane
 */
public class TagIdentiy {
    @Trim
    @Parsed(field = "idt_id")
    private String idt_id;
    @Trim
    @Parsed(field = "idt_name")
    private String nomClient;
    @Trim
    @Parsed(field = "idt_firstname")
    private String prenomClient;
    @Trim
    @Parsed(field = "idt_info")
    private String infoClient;
    @Trim
    @Parsed(field = "idt_pincode")
    private String pinCodeClient;
    @Trim
    @Parsed(field = "idt_password")
    private String passwordClient;
    @Trim
    @Parsed(field = "idt_role")
    private String roleClient;
    @Trim
    @Parsed(field = "idt_email")
    private String emailClient;
    @Trim
    @Parsed(field = "idt_address")
    private String adresseClient;
    @Trim
    @Parsed(field = "idt_city")
    private String villeClient;
    @Trim
    @Parsed(field = "idt_zipcode")
    private String codePaysTelephone;
    @Trim
    @Parsed(field = "idt_nationality")
    private String nationaliteClient;
    @Trim
    @Parsed(field = "idt_idnumber")
    private String idNumberClient;
    @Trim
    @Parsed(field = "idt_contactphone")
    private String personneAprevenir;
    @Trim
    @Parsed(field = "idt_birthdate")
    private String dateNaissanceClient;
    @Trim
    @Parsed(field = "idt_birthplace")
    private String lieuNaissanceClient;
    @Trim
    @Parsed(field = "idt_gender")
    private String sexeClient;
    @Trim
    @Parsed(field = "idt_region")
    private String regionClient;
    @Trim
    @Parsed(field = "idt_bank_name")
    private String nomBanqueClient;
    @Trim
    @Parsed(field = "idt_district")
    private String districtClient;
    @Trim
    @Parsed(field = "idt_province")
    private String provinceClient;
    @Trim
    @Parsed(field = "idt_lat")
    private String idt_lat;
    @Trim
    @Parsed(field = "idt_lng")
    private String idt_lng;
    @Trim
    @Parsed(field = "idt_parent_id")
    private String idt_parent_id;
    @Trim
    @Parsed(field = "idt_maiden_name")
    private String nomJeuneFille;

    @Trim
    @Parsed(field = "idt_status")
    private String idt_status;


    public String getIdt_id() {
        return idt_id;
    }

    public void setIdt_id(String idt_id) {
        this.idt_id = idt_id;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getPrenomClient() {
        return prenomClient;
    }

    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }

    public String getInfoClient() {
        return infoClient;
    }

    public void setInfoClient(String infoClient) {
        this.infoClient = infoClient;
    }

    public String getPinCodeClient() {
        return pinCodeClient;
    }

    public void setPinCodeClient(String pinCodeClient) {
        this.pinCodeClient = pinCodeClient;
    }

    public String getPasswordClient() {
        return passwordClient;
    }

    public void setPasswordClient(String passwordClient) {
        this.passwordClient = passwordClient;
    }

    public String getRoleClient() {
        return roleClient;
    }

    public void setRoleClient(String roleClient) {
        this.roleClient = roleClient;
    }

    public String getEmailClient() {
        return emailClient;
    }

    public void setEmailClient(String emailClient) {
        this.emailClient = emailClient;
    }

    public String getAdresseClient() {
        return adresseClient;
    }

    public void setAdresseClient(String adresseClient) {
        this.adresseClient = adresseClient;
    }

    public String getVilleClient() {
        return villeClient;
    }

    public void setVilleClient(String villeClient) {
        this.villeClient = villeClient;
    }

    public String getCodePaysTelephone() {
        return codePaysTelephone;
    }

    public void setCodePaysTelephone(String codePaysTelephone) {
        this.codePaysTelephone = codePaysTelephone;
    }

    public String getNationaliteClient() {
        return nationaliteClient;
    }

    public void setNationaliteClient(String nationaliteClient) {
        this.nationaliteClient = nationaliteClient;
    }

    public String getIdNumberClient() {
        return idNumberClient;
    }

    public void setIdNumberClient(String idNumberClient) {
        this.idNumberClient = idNumberClient;
    }

    public String getPersonneAprevenir() {
        return personneAprevenir;
    }

    public void setPersonneAprevenir(String personneAprevenir) {
        this.personneAprevenir = personneAprevenir;
    }

    public String getDateNaissanceClient() {
        return dateNaissanceClient;
    }

    public void setDateNaissanceClient(String dateNaissanceClient) {
        this.dateNaissanceClient = dateNaissanceClient;
    }

    public String getLieuNaissanceClient() {
        return lieuNaissanceClient;
    }

    public void setLieuNaissanceClient(String lieuNaissanceClient) {
        this.lieuNaissanceClient = lieuNaissanceClient;
    }

    public String getSexeClient() {
        return sexeClient;
    }

    public void setSexeClient(String sexeClient) {
        this.sexeClient = sexeClient;
    }

    public String getRegionClient() {
        return regionClient;
    }

    public void setRegionClient(String regionClient) {
        this.regionClient = regionClient;
    }

    public String getNomBanqueClient() {
        return nomBanqueClient;
    }

    public void setNomBanqueClient(String nomBanqueClient) {
        this.nomBanqueClient = nomBanqueClient;
    }

    public String getDistrictClient() {
        return districtClient;
    }

    public void setDistrictClient(String districtClient) {
        this.districtClient = districtClient;
    }

    public String getProvinceClient() {
        return provinceClient;
    }

    public void setProvinceClient(String provinceClient) {
        this.provinceClient = provinceClient;
    }

    public String getIdt_lat() {
        return idt_lat;
    }

    public void setIdt_lat(String idt_lat) {
        this.idt_lat = idt_lat;
    }

    public String getIdt_lng() {
        return idt_lng;
    }

    public void setIdt_lng(String idt_lng) {
        this.idt_lng = idt_lng;
    }

    public String getIdt_parent_id() {
        return idt_parent_id;
    }

    public void setIdt_parent_id(String idt_parent_id) {
        this.idt_parent_id = idt_parent_id;
    }

    public String getIdt_status() {
        return idt_status;
    }

    public void setIdt_status(String idt_status) {
        this.idt_status = idt_status;
    }

    public String getNomJeuneFille() {
        return nomJeuneFille;
    }

    public void setNomJeuneFille(String nomJeuneFille) {
        this.nomJeuneFille = nomJeuneFille;
    }

}
