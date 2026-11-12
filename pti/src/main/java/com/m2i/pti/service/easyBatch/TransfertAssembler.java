/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.easyBatch;

/**
 *
 * @author cda
 */
public class TransfertAssembler implements CsvAssembler<CSVTransfert> {

    @Override
    public CSVTransfert assemble(String[] cols) {

        CSVTransfert csvTransfert = new CSVTransfert();
        csvTransfert.setNom(cols[0]);
        csvTransfert.setPrenom(cols[1]);
        csvTransfert.setNumeroTelephone(cols[2]);
        csvTransfert.setMontant(Double.parseDouble(cols[3]));
        csvTransfert.setNarration(cols[4]);
        return csvTransfert;

    }

}
