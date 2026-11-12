package com.m2i.pti.service.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StatsBilanMensuel {
    List<Integer> months = new ArrayList<>();
    List<Object> datas=new ArrayList<>();
    List<String> labels=new ArrayList<>();

    public List<Integer> getMonths() {
        return months;
    }

    public void setMonths(List<Integer> months) {
        this.months = months;
    }

    public List<Object> getDatas() {
        return datas;
    }

    public void setDatas(List<Object> datas) {
        this.datas = datas;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}
