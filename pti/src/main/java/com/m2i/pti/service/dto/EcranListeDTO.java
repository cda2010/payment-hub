package com.m2i.pti.service.dto;

public class EcranListeDTO {

    private Long id;
    private Integer numeroOrdre;

    public EcranListeDTO(Long id, Integer numeroOrdre) {
        this.id = id;
        this.numeroOrdre = numeroOrdre;
    }

    public Long getId() {
        return id;
    }

    public Integer getNumeroOrdre() {
        return numeroOrdre;
    }
}
