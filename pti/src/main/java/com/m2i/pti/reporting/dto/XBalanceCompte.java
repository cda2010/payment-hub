package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;
import java.util.List;

public class XBalanceCompte {
    public String titre;
    private BigDecimal sommeDebit;
    private BigDecimal sommeCredit;
    private BigDecimal sommeSoldeD;
    public List<BalanceCptClient> balanceCptClients;
    public BigDecimal getSommeDebit() {
        return sommeDebit;
    }

    public void setSommeDebit(BigDecimal sommeDebit) {
        this.sommeDebit = sommeDebit;
    }

    public BigDecimal getSommeCredit() {
        return sommeCredit;
    }

    public void setSommeCredit(BigDecimal sommeCredit) {
        this.sommeCredit = sommeCredit;
    }

    public BigDecimal getSommeSoldeD() {
        return sommeSoldeD;
    }

    public void setSommeSoldeD(BigDecimal sommeSoldeD) {
        this.sommeSoldeD = sommeSoldeD;
    }

    public BigDecimal getSommeSoldeC() {
        return sommeSoldeC;
    }

    public void setSommeSoldeC(BigDecimal sommeSoldeC) {
        this.sommeSoldeC = sommeSoldeC;
    }

    private BigDecimal sommeSoldeC;
    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }



    public List<BalanceCptClient> getBalanceCptClients() {
        return balanceCptClients;
    }

    public void setBalanceCptClients(List<BalanceCptClient> balanceCptClients) {
        this.balanceCptClients = balanceCptClients;
    }
}
