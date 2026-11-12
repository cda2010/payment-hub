/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;



/**
 *
 * @author cda
 */
public class MDocument {
   
    private byte[] valeur;
    private String valeurContentType;
    private String typeDocument;


    public byte[] getValeur() {
        return valeur;
    }

    public void setValeur(byte[] valeur) {
        this.valeur = valeur;
    }

    public String getValeurContentType() {
        return valeurContentType;
    }

    public void setValeurContentType(String valeurContentType) {
        this.valeurContentType = valeurContentType;
    }

    public String getTypeDocument() {
        return typeDocument;
    }

    public void setTypeDocument(String typeDocument) {
        this.typeDocument = typeDocument;
    }

    
    
}
