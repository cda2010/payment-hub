package com.m2i.pti.mobile.dto;


import com.m2i.pti.domain.Facturier;
import com.m2i.pti.domain.enumeration.TypeFacturier;
import java.io.Serializable;
import jakarta.persistence.Lob;

public class MFacturier implements Serializable {

    private Long id;
    private String nomFacturier;
    private String codeFacturier;
    private byte[] image;
    private String valeurContentType;
    private String codeUo;

    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }

  

    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomFacturier() {
        return nomFacturier;
    }

    public void setNomFacturier(String nomFacturier) {
        this.nomFacturier = nomFacturier;
    }

    public String getCodeFacturier() {
        return codeFacturier;
    }

    public void setCodeFacturier(String codeFacturier) {
        this.codeFacturier = codeFacturier;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getValeurContentType() {
        return valeurContentType;
    }

    public void setValeurContentType(String valeurContentType) {
        this.valeurContentType = valeurContentType;
    }

    
  
}

