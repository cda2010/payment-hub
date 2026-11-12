package com.m2i.pti.service.dto;


import com.m2i.omnicanal.outils.util.StandardResponse;

import java.util.ArrayList;

public class FournisseurDTO extends StandardResponse {
    private ArrayList<FseDTO> list;

    public ArrayList<FseDTO> getList() {
        return list;
    }

    public void setList(ArrayList<FseDTO> list) {
        this.list = list;
    }
}
