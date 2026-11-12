/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.model;

import com.m2i.pti.mobile.dto.MParametre;
import java.util.List;

/**
 *
 * @author cda
 */
public class GlobalData {
    String tableName;
    List<MParametre> listeData;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<MParametre> getListeData() {
        return listeData;
    }

    public void setListeData(List<MParametre> listeData) {
        this.listeData = listeData;
    }

}
