package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.PointLot;
import com.gateway.payment.fidelite.repository.PointLotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Fait périr les lots échus et prévient avant l'échéance, selon le consentement. */
@Service
public class ExpirationService {

    private final PointLotRepository lots;
    private final LedgerService ledger;
    private final NotificationService notifications;

    public ExpirationService(PointLotRepository lots, LedgerService ledger, NotificationService notifications) {
        this.lots = lots;
        this.ledger = ledger;
        this.notifications = notifications;
    }

    @Transactional
    public int expirer(Instant maintenant) {
        int compte = 0;
        for (PointLot lot : lots.expires(maintenant)) {
            long retires = ledger.expirer(lot.getId());
            if (retires <= 0 || lot.getAccount() == null) {
                continue;
            }
            notifications.prevenir(
                    lot.getTenantId(),
                    lot.getAccount().getPartyId(),
                    lot.getAccount().getProgramme() == null ? null : lot.getAccount().getProgramme().getId(),
                    "EXPIRATION",
                    "EXPIRATION:" + lot.getId(),
                    retires + " points ont expiré");
            compte++;
        }
        return compte;
    }

    @Transactional
    public int prevenir(Instant maintenant) {
        int compte = 0;
        compte += prevenirEntre(maintenant, maintenant.plus(1, ChronoUnit.DAYS), "1");
        compte += prevenirEntre(maintenant, maintenant.plus(7, ChronoUnit.DAYS), "7");
        return compte;
    }

    private int prevenirEntre(Instant debut, Instant fin, String horizon) {
        int compte = 0;
        for (PointLot lot : lots.expirantEntre(debut, fin)) {
            if (lot.getAccount() == null) {
                continue;
            }
            notifications.prevenir(
                    lot.getTenantId(),
                    lot.getAccount().getPartyId(),
                    lot.getAccount().getProgramme() == null ? null : lot.getAccount().getProgramme().getId(),
                    "EXPIRATION",
                    "EXPIRATION:" + lot.getId() + ":" + horizon,
                    "Des points expirent dans moins de " + horizon + " jour" + ("1".equals(horizon) ? "" : "s"));
            compte++;
        }
        return compte;
    }

    public long annuler(String tenantId, String reference, UUID evenementId) {
        if (evenementId != null) {
            return ledger.annulerEvenement(tenantId, evenementId);
        }
        return ledger.annuler(tenantId, reference);
    }
}
