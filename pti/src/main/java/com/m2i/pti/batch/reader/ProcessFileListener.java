/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.m2i.pti.batch.reader;

import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.service.easyBatch.CSVTransfert;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;

/**
 *
 * @author USER_M2I
 */
public class ProcessFileListener implements ItemProcessListener<CSVTransfert, LigneChargementTransfert>{

    @Override
    public void beforeProcess(CSVTransfert t) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void afterProcess(CSVTransfert t, LigneChargementTransfert s) {
        
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void onProcessError(CSVTransfert t, Exception excptn) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
