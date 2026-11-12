package com.m2i.pti.service.dto;

import java.util.List;

public class Result {
    String msg;
    String codeErr;
    private String text;
    InfoClientCBI infoClient;
    private String desc;
    private List<Mouvement> listeMouvement;

    public List<Mouvement> getListeMouvement() {
        return listeMouvement;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setListeMouvement(List<Mouvement> listeMouvement) {
        this.listeMouvement = listeMouvement;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCodeErr() {
        return codeErr;
    }

    public void setCodeErr(String codeErr) {
        this.codeErr = codeErr;
    }

    public InfoClientCBI getInfoClient() {
        return infoClient;
    }

    public void setInfoClient(InfoClientCBI infoClient) {
        this.infoClient = infoClient;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    

    @Override
    public String toString() {
        return "Result{" +
                "msg='" + msg + '\'' +
                ", codeErr='" + codeErr + '\'' +
                ", text='" + text + '\'' +
                ", infoClient=" + infoClient +
                ", desc='" + desc + '\'' +
                ", listeMouvement=" + listeMouvement +
                '}';
    }
}
