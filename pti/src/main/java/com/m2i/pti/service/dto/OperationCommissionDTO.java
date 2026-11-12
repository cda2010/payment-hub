/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;
import com.m2i.pti.domain.UniteOrganisation;
import java.io.Serializable;
/**
 *
 * @author cda
 */
public class OperationCommissionDTO  implements Serializable{
   Long uoId;
    Long operationId;

   

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Long getUoId() {
        return uoId;
    }

    public void setUoId(Long uoId) {
        this.uoId = uoId;
    }

    @Override
    public String toString() {
        return "OperationCommissionDTO{" + "uoId=" + uoId + ", operationId=" + operationId + '}';
    }

   
    
}
