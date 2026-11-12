package com.m2i.pti.reporting.dto;

import java.util.List;

public class OperationList {
    public List<XTransactionOp> listOperation;
    public String logo;
    public String fmt;
    public List<XTransactionOp> getListOperation() {
        return listOperation;
    }

    public void setListOperation(List<XTransactionOp> listOperation) {
        this.listOperation = listOperation;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getFmt() {
        return fmt;
    }

    public void setFmt(String fmt) {
        this.fmt = fmt;
    }
}
