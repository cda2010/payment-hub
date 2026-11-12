package com.gateway.payment.common.domain;

/**
 * Catalogue TypeMethode Omnicanal (référence pour la création de méthodes payment-2).
 */
public enum OmnicanalTypeMethode {
    GETCARDLESSTOKEN,
    TRANSACTIONSTATUS,
    PING,
    SENDFILEMAIL,
    SENDFILEFTP,
    GETDATA,
    SETTLEMENT,
    ENVOIMAIL,
    GETINFOS,
    GETAIRTIME,
    DEBIT,
    CREDIT,
    CONSULTATION,
    NOP,
    TRANSFERT,
    CONSULTATIONSOLDE,
    CONSULTATIONHIST,
    ENVOISMS,
    LISTEVENEMENT,
    INFOCLIENT,
    CARTEMOUVEMENT,
    GETMONTANT,
    CONSULTATIONFACTURE,
    VERIFREGLEFACTURIER,
    /** Liste catalogue UI (présentation) — réponse items → CatalogItemDto. */
    DATASOURCE
}
