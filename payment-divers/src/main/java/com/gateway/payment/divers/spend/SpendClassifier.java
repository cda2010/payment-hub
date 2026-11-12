package com.gateway.payment.divers.spend;

import com.gateway.payment.divers.repository.CategorieDepenseRepository;
import com.gateway.payment.spend.kafka.SpendEvent;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Un paiement réussi devient une dépense seulement s'il consomme
 * (facture, marchand, paiement). Le virement et le solde restent dehors.
 * Le code retenu existe dans {@code categorie_depense}.
 */
@Component
public class SpendClassifier {

    private static final Set<String> SPEND_TYPES = Set.of("PAYEMENT", "PAIEMENT_FACTURE", "DEFAUT");

    private final CategorieDepenseRepository categories;

    public SpendClassifier(CategorieDepenseRepository categories) {
        this.categories = categories;
    }

    public Optional<String> classify(SpendEvent event) {
        if (event == null || event.partyId() == null || event.paymentId() == null || event.paymentId().isBlank()) {
            return Optional.empty();
        }
        if (event.amount() == null || event.amount().signum() <= 0) {
            return Optional.empty();
        }
        String type = token(event.serviceType()).toUpperCase(Locale.ROOT);
        if (!SPEND_TYPES.contains(type)) {
            return Optional.empty();
        }
        String hint = token(event.categorieHint()).toLowerCase(Locale.ROOT);
        if (categories.existsById(hint)) {
            return Optional.of(hint);
        }
        if ("PAIEMENT_FACTURE".equals(type) || looksLikeBill(event.productCode())) {
            return Optional.of("factures");
        }
        return Optional.of("autre");
    }

    private static boolean looksLikeBill(String productCode) {
        String code = token(productCode).toUpperCase(Locale.ROOT);
        return code.contains("FACTURE") || code.contains("BILL") || code.contains("AIRTIME");
    }

    private static String token(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replace('-', '_').replace(' ', '_');
    }
}
