package com.gateway.payment.divers.service;

import com.gateway.payment.divers.domain.CategorieDepense;
import com.gateway.payment.divers.domain.Depense;
import com.gateway.payment.divers.repository.CategorieDepenseRepository;
import com.gateway.payment.divers.repository.DepenseRepository;
import com.gateway.payment.divers.spend.SpendClassifier;
import com.gateway.payment.spend.kafka.SpendEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class DepenseService {

    private static final Logger log = LoggerFactory.getLogger(DepenseService.class);

    private final DepenseRepository depenses;
    private final CategorieDepenseRepository categories;
    private final SpendClassifier classifier;

    public DepenseService(
            DepenseRepository depenses,
            CategorieDepenseRepository categories,
            SpendClassifier classifier
    ) {
        this.depenses = depenses;
        this.categories = categories;
        this.classifier = classifier;
    }

    @Transactional
    public void record(SpendEvent event) {
        String categorie = classifier.classify(event).orElse(null);
        if (categorie == null) {
            return;
        }
        if (depenses.existsByPaymentId(event.paymentId())) {
            return;
        }
        CategorieDepense type = categories.findById(categorie).orElse(null);
        if (type == null) {
            log.warn("Catégorie {} absente, paiement {} ignoré", categorie, event.paymentId());
            return;
        }
        Depense row = new Depense();
        row.setPaymentId(event.paymentId().trim());
        row.setPartyId(event.partyId());
        row.setTenantId(blank(event.tenantId()));
        row.setCategorie(type);
        row.setAmount(event.amount());
        row.setCurrency(event.currency() == null || event.currency().isBlank() ? "XOF" : event.currency().trim());
        row.setMerchantName(blank(event.merchantName()));
        row.setProductCode(blank(event.productCode()));
        row.setOccurredAt(event.occurredAt() != null ? event.occurredAt() : Instant.now());
        depenses.save(row);
        log.info("Dépense {} classée {} pour {}", row.getPaymentId(), categorie, row.getPartyId());
    }

    private static String blank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
