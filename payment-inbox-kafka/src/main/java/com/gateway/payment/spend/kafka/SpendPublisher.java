package com.gateway.payment.spend.kafka;

/** Écriture Kafka d'un paiement réussi. Ne touche pas à la table {@code depense}. */
public interface SpendPublisher {

    void publish(SpendEvent event);
}
