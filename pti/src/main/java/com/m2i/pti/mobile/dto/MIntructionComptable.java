/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import com.m2i.pti.domain.InstructionComptable;
import java.util.List;

/**
 *
 * @author cda
 */
public class MIntructionComptable {
  List<InstructionComptable>   listInstructionComptable ;

    public MIntructionComptable() {
    }

    public MIntructionComptable(List<InstructionComptable> listInstructionComptable) {
        this.listInstructionComptable = listInstructionComptable;
    }

    public List<InstructionComptable> getListInstructionComptable() {
        return listInstructionComptable;
    }

    public void setListInstructionComptable(List<InstructionComptable> listInstructionComptable) {
        this.listInstructionComptable = listInstructionComptable;
    }
  

   
    
}
