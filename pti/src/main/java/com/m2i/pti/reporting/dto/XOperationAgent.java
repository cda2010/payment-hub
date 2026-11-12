package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;
import java.util.List;

public class XOperationAgent {
    private Double total;
    private String uoEmetteur;

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getUoEmetteur() {
        return uoEmetteur;
    }

    public void setUoEmetteur(String uoEmetteur) {
        this.uoEmetteur = uoEmetteur;
    }
}
