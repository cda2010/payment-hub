package com.m2i.pti.reporting.dto;
import com.m2i.pti.service.dto.TransactionComptePtiDTO;
import java.util.List;

public class EtatItems {
    String format;
    List<TransactionComptePtiDTO> data;
    String numeroCompte;
    String DateDebut;
    String DateFin;



    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<TransactionComptePtiDTO> getData() {
        return this.data;
    }

    public void setData(List<TransactionComptePtiDTO> data) {
        this.data = data;
    }

    public String getNumeroCompte() {
        return this.numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public String getDateDebut() {
        return this.DateDebut;
    }

    public void setDateDebut(String DateDebut) {
        this.DateDebut = DateDebut;
    }

    public String getDateFin() {
        return this.DateFin;
    }

    public void setDateFin(String DateFin) {
        this.DateFin = DateFin;
    }


    @Override
    public String toString() {
        return "{" +
            " format='" + getFormat() + "'" +
            ", data='" + getData() + "'" +
            ", numeroCompte='" + getNumeroCompte() + "'" +
            ", DateDebut='" + getDateDebut() + "'" +
            ", DateFin='" + getDateFin() + "'" +
            "}";
    }
    


}