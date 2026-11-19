package com.gateway.payment.identity.enrolement;

import java.util.UUID;

/** Dépose une pièce d'enrôlement dans payment-documents. */
public interface CoffreDocuments {

    UUID deposer(String tenantId, UUID dossierId, String face, String typeDocument, String contentType, byte[] contenu);

    /** Le document déposé sur le dossier devient celui de la personne. */
    void attribuer(UUID documentId, UUID partyId);
}
