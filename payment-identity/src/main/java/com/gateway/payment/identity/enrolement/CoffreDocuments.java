package com.gateway.payment.identity.enrolement;

import java.util.UUID;

/** Dépose une pièce d'enrôlement dans payment-documents. */
public interface CoffreDocuments {

    UUID deposer(String tenantId, UUID dossierId, String face, String typeDocument, String contentType, byte[] contenu);
}
