package com.gateway.payment.bpm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Les champs déjà renseignés sont retirés du formulaire servi. Le montant échappe
 * à cette règle tant qu'il vaut zéro : les produits qui le collectent en cours de
 * parcours (facture, ACCOUNT_TRANSFER/CAPTURE_AMOUNT) démarrent à zéro, et masquer
 * le champ viderait l'étape chargée de le saisir.
 */
class BpmCheckoutFilterCollectedTest {

    private final BpmCheckoutNextActionService service = new BpmCheckoutNextActionService(
            null, null, null, null, null, null, new ObjectMapper(), null, null, null, null);

    private static Map<String, Object> amountForm() {
        return Map.of("components", List.of(
                Map.of("type", "number", "key", "amount", "label", "Montant"),
                Map.of("type", "textfield", "key", "currency", "label", "Devise")
        ));
    }

    private static PaymentTransaction transaction(BigDecimal amount, String contextJson) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setAmount(amount);
        tx.setContextJson(contextJson);
        return tx;
    }

    @SuppressWarnings("unchecked")
    private static List<String> keys(Map<String, Object> schema) {
        return ((List<Map<String, Object>>) schema.get("components")).stream()
                .map(c -> String.valueOf(c.get("key")))
                .toList();
    }

    @Test
    void zero_amount_stays_editable() {
        Map<String, Object> filtered = service.filterCollected(
                amountForm(), transaction(BigDecimal.ZERO, "{\"amount\":0.0,\"currency\":\"XOF\"}"));

        assertThat(keys(filtered)).containsExactly("amount");
    }

    @Test
    void captured_amount_is_hidden() {
        Map<String, Object> filtered = service.filterCollected(
                amountForm(), transaction(new BigDecimal("150000"), "{\"amount\":150000,\"currency\":\"XOF\"}"));

        assertThat(keys(filtered)).isEmpty();
    }
}
