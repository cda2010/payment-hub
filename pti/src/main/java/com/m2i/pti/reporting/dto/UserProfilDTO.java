package com.m2i.pti.reporting.dto;

import com.m2i.pti.service.dto.ProfilDTO;

import java.util.List;

public class UserProfilDTO {
    private List<XProfilDTO> listProfil;
    private String titre;
    private String logo;
    public List<XProfilDTO> getListProfil() {
        return listProfil;
    }

    public void setListProfil(List<XProfilDTO> listProfil) {
        this.listProfil = listProfil;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Override
    public String toString() {
        return "UserProfilDTO{" +
                "listProfil=" + listProfil +
                '}';
    }
}
