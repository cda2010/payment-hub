package com.m2i.pti.domain;

import com.m2i.pti.service.dto.DonneeDTO;

import java.util.ArrayList;
import java.util.List;

public class LigneGroupeProfilTemplate {

    List<DonneeDTO> tableau = new ArrayList<>() ;

    public List<DonneeDTO> getTableau() {
        return tableau;
    }

    public void setTableau(List<DonneeDTO> tableau) {
        this.tableau = tableau;
    }
}
