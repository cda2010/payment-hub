package com.gateway.payment.identity.enrolement;

import java.util.List;

/** Pièces à demander, décidées par la DMN d'enrôlement. */
public interface PiecesEnrolement {

    List<ParcoursPays.Document> demander(String pays, String typeDocument);
}
