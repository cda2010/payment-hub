package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.Consentement;
import com.gateway.payment.fidelite.domain.NotificationFidelite;
import com.gateway.payment.fidelite.repository.ConsentementRepository;
import com.gateway.payment.fidelite.repository.NotificationFideliteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Enregistre un message par canal consenti. Aucun envoi push, SMS, e-mail ou WhatsApp :
 * la ligne reste à envoyer pour le connecteur de notification.
 */
@Service
public class NotificationService {

    private final ConsentementRepository consentements;
    private final NotificationFideliteRepository notifications;

    public NotificationService(ConsentementRepository consentements, NotificationFideliteRepository notifications) {
        this.consentements = consentements;
        this.notifications = notifications;
    }

    @Transactional
    public Consentement enregistrerConsentement(
            String tenantId, UUID partyId, boolean push, boolean sms, boolean email, boolean whatsapp
    ) {
        Consentement row = consentements.findByTenantIdAndPartyId(tenantId, partyId).orElseGet(Consentement::new);
        row.setTenantId(tenantId);
        row.setPartyId(partyId);
        row.setPush(push);
        row.setSms(sms);
        row.setEmail(email);
        row.setWhatsapp(whatsapp);
        return consentements.save(row);
    }

    @Transactional
    public void prevenir(String tenantId, UUID partyId, UUID programmeId, String type, String dedupKey, String message) {
        if (tenantId == null || partyId == null || dedupKey == null || message == null || message.isBlank()) {
            return;
        }
        Consentement consentement = consentements.findByTenantIdAndPartyId(tenantId, partyId).orElse(null);
        if (consentement == null) {
            return;
        }
        String texte = message.length() > 400 ? message.substring(0, 400) : message;
        for (String canal : canaux(consentement)) {
            if (notifications.existsByTenantIdAndDedupKeyAndCanal(tenantId, dedupKey, canal)) {
                continue;
            }
            NotificationFidelite row = new NotificationFidelite();
            row.setTenantId(tenantId);
            row.setPartyId(partyId);
            row.setProgrammeId(programmeId);
            row.setType(type);
            row.setCanal(canal);
            row.setMessage(texte);
            row.setStatut("A_ENVOYER");
            row.setDedupKey(dedupKey);
            row.setCreatedAt(Instant.now());
            notifications.save(row);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationFidelite> duClient(String tenantId, UUID partyId) {
        return notifications.findByTenantIdAndPartyIdOrderByCreatedAtDesc(tenantId, partyId);
    }

    @Transactional(readOnly = true)
    public List<NotificationFidelite> duTenant(String tenantId) {
        return notifications.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    private static List<String> canaux(Consentement consentement) {
        List<String> canaux = new ArrayList<>();
        if (consentement.isPush()) {
            canaux.add("PUSH");
        }
        if (consentement.isSms()) {
            canaux.add("SMS");
        }
        if (consentement.isEmail()) {
            canaux.add("EMAIL");
        }
        if (consentement.isWhatsapp()) {
            canaux.add("WHATSAPP");
        }
        return canaux;
    }
}
