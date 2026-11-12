/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.m2i.pti.batch.reader;

import com.m2i.pti.service.easyBatch.CSVTransfert;
import org.springframework.batch.core.ItemReadListener;

/**
 *
 * @author USER_M2I
 */
public class ReadFileListner implements ItemReadListener<CSVTransfert>{
    @Override
    public void afterRead(CSVTransfert csvTransfert) {
        System.out.println("Client : " + csvTransfert.getMontant());
    }

    @Override
    public void beforeRead() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void onReadError(Exception excptn) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
