/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import java.util.List;

/**
 *
 * @author hp
 */
public class StructureComptabilisation {
    String compteDebiteur;
    String codeAgenceDebit;
    String nomDonneurOrdre;
    String codeBankDebit;
    List<DonneeVirement> listDonneeVirement;

    public String getCompteDebiteur() {
        return compteDebiteur;
    }

    public void setCompteDebiteur(String compteDebiteur) {
        this.compteDebiteur = compteDebiteur;
    }

    public String getCodeAgenceDebit() {
        return codeAgenceDebit;
    }

    public void setCodeAgenceDebit(String codeAgenceDebit) {
        this.codeAgenceDebit = codeAgenceDebit;
    }

    public String getNomDonneurOrdre() {
        return nomDonneurOrdre;
    }

    public void setNomDonneurOrdre(String nomDonneurOrdre) {
        this.nomDonneurOrdre = nomDonneurOrdre;
    }

    public String getCodeBankDebit() {
        return codeBankDebit;
    }

    public void setCodeBankDebit(String codeBankDebit) {
        this.codeBankDebit = codeBankDebit;
    }

    public List<DonneeVirement> getListDonneeVirement() {
        return listDonneeVirement;
    }

    public void setListDonneeVirement(List<DonneeVirement> listDonneeVirement) {
        this.listDonneeVirement = listDonneeVirement;
    }
}
