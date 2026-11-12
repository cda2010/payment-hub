/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

/**
 *
 * @author cda
 */
public class MessageRet {
    String msg;
  String codeErr;
    String text;
  
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

   
    
    
}
