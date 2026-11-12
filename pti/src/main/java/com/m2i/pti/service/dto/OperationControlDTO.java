/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import com.m2i.pti.mobile.dto.MParametre;
import java.util.List;

/**
 *
 * @author cda
 */
public class OperationControlDTO {
    String codeOperation;
    String codeFse;
 List<MParametre> extraData ;
    public String getCodeOperation() {
        return codeOperation;
    }
    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }
    public String getCodeFse() {
        return codeFse;
    }
    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public List<MParametre> getExtraData() {
        return extraData;
    }

    public void setExtraData(List<MParametre> extraData) {
        this.extraData = extraData;
    }
    
    
}
