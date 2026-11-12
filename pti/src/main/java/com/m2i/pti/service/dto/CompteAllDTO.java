package com.m2i.pti.service.dto;

import com.m2i.pti.domain.CompteAutre;
import com.m2i.pti.domain.CompteBanque;

public class CompteAllDTO {
    private CompteDTO compteDTO;
    private CompteAutreDTO compteAutreDTO;
    private CompteBanqueDTO compteBanqueDTO;


    public CompteDTO getCompteDTO() {
        return compteDTO;
    }

    public void setCompteDTO(CompteDTO compteDTO) {
        this.compteDTO = compteDTO;
    }

    public CompteAutreDTO getCompteAutreDTO() {
        return compteAutreDTO;
    }

    public void setCompteAutreDTO(CompteAutreDTO compteAutreDTO) {
        this.compteAutreDTO = compteAutreDTO;
    }

    public CompteBanqueDTO getCompteBanqueDTO() {
        return compteBanqueDTO;
    }

    public void setCompteBanqueDTO(CompteBanqueDTO compteBanqueDTO) {
        this.compteBanqueDTO = compteBanqueDTO;
    }
}
