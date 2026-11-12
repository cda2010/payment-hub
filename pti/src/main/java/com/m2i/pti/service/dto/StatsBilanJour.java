package com.m2i.pti.service.dto;

import java.util.ArrayList;
import java.util.List;

public class StatsBilanJour{
List<String> labels=new ArrayList<>();
List<Double> datas=new ArrayList<>();

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Double> getDatas() {
        return datas;
    }

    public void setDatas(List<Double> datas) {
        this.datas = datas;
    }
}
