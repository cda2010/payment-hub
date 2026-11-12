package com.m2i.pti.reporting.dto;

import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.TransactionComptePti;

public class ListTransactionDTO {
    private TransactionComptePti transactionComptePti;
    private Compte compte;
    private Operation operation;

    public TransactionComptePti getTransactionComptePti() {
        return transactionComptePti;
    }

    public void setTransactionComptePti(TransactionComptePti transactionComptePti) {
        this.transactionComptePti = transactionComptePti;
    }

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}
