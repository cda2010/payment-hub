package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.util.List;

public class XListTransactionCompte extends ReportHeader {
    private List<XTransactionCompteDTO> listTransaction;

    public List<XTransactionCompteDTO> getListTransaction() {
        return listTransaction;
    }

    public void setListTransaction(List<XTransactionCompteDTO> listTransaction) {
        this.listTransaction = listTransaction;
    }
}
