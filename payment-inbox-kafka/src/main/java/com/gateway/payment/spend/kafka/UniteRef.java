package com.gateway.payment.spend.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Unité d'organisation telle qu'elle est au moment du paiement : elle-même ou un ancêtre. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UniteRef(String id, String type) {
}
