package com.gateway.payment.identity.service;

import com.gateway.payment.identity.domain.IdentityAppareil;
import com.gateway.payment.identity.domain.IdentityAppareil.Plateforme;
import com.gateway.payment.identity.domain.IdentityAppareil.Statut;
import com.gateway.payment.identity.domain.IdentityChannelUser;
import com.gateway.payment.identity.repository.IdentityAppareilRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Enregistre l'appareil après la preuve du mot de passe et ferme sa session Keycloak à la révocation. */
@Service
public class AppareilService {

    private final IdentityAppareilRepository appareils;
    private final SessionsKeycloak sessions;

    public AppareilService(IdentityAppareilRepository appareils, SessionsKeycloak sessions) {
        this.appareils = appareils;
        this.sessions = sessions;
    }

    @Transactional(readOnly = true)
    public List<Vue> lister(UUID partyId, String sessionId) {
        return appareils.findByPartyIdOrderByLastSeenAtDesc(partyId).stream()
                .filter(appareil -> appareil.getStatut() == Statut.ACTIF)
                .sorted(Comparator.comparing((IdentityAppareil appareil) -> courant(appareil, sessionId)).reversed()
                        .thenComparing(IdentityAppareil::getLastSeenAt, Comparator.reverseOrder()))
                .map(appareil -> vue(appareil, sessionId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Vue> listerParty(UUID partyId) {
        return appareils.findByPartyIdOrderByLastSeenAtDesc(partyId).stream()
                .map(appareil -> vue(appareil, null))
                .toList();
    }

    @Transactional
    public Vue enregistrer(IdentityChannelUser utilisateur, String sessionId, String appareilId, String nom, String plateforme) {
        String identifiant = appareil(appareilId);
        String libelle = requis(nom, "Le nom de l'appareil est requis");
        if (libelle.length() > 120) {
            libelle = libelle.substring(0, 120);
        }
        Plateforme support = plateforme(plateforme);
        IdentityAppareil fiche = appareils.findByTenantIdAndAppareilId(utilisateur.getTenantId(), identifiant)
                .orElseGet(IdentityAppareil::new);
        String ancienneSession = fiche.getSessionId();
        boolean autrePersonne = fiche.getPartyId() != null && !fiche.getPartyId().equals(utilisateur.getPartyId());
        fiche.setTenantId(utilisateur.getTenantId());
        fiche.setPartyId(utilisateur.getPartyId());
        fiche.setUsername(utilisateur.getUsername());
        fiche.setAppareilId(identifiant);
        fiche.setNom(libelle);
        fiche.setPlateforme(support);
        fiche.setStatut(Statut.ACTIF);
        fiche.setSessionId(sessionId);
        fiche.setLastSeenAt(Instant.now());
        IdentityAppareil sauve = appareils.save(fiche);
        if (sessionId != null && !sessionId.isBlank()) {
            for (IdentityAppareil autre : appareils.findByPartyIdOrderByLastSeenAtDesc(utilisateur.getPartyId())) {
                if (identifiant.equals(autre.getAppareilId()) || !sessionId.equals(autre.getSessionId())) {
                    continue;
                }
                autre.setSessionId(null);
                appareils.save(autre);
            }
        }
        if (autrePersonne || (ancienneSession != null && !ancienneSession.equals(sessionId))) {
            fermerAncienne(ancienneSession);
        }
        return vue(sauve, sessionId);
    }

    @Transactional
    public void revoquer(IdentityChannelUser utilisateur, UUID id, String sessionId, boolean oubli) {
        IdentityAppareil fiche = appareils.findById(id)
                .filter(appareil -> utilisateur.getPartyId().equals(appareil.getPartyId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appareil introuvable"));
        if (!oubli && fiche.getSessionId() != null && fiche.getSessionId().equals(sessionId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cet appareil est celui de la session");
        }
        if (fiche.getStatut() == Statut.REVOQUE) {
            return;
        }
        fermer(fiche.getSessionId());
        fiche.setStatut(Statut.REVOQUE);
        fiche.setSessionId(null);
        appareils.save(fiche);
    }

    @Transactional
    public void revoquerPourParty(UUID partyId, UUID id) {
        IdentityAppareil fiche = appareils.findById(id)
                .filter(appareil -> partyId.equals(appareil.getPartyId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appareil introuvable"));
        if (fiche.getStatut() == Statut.REVOQUE) {
            return;
        }
        fermer(fiche.getSessionId());
        fiche.setStatut(Statut.REVOQUE);
        fiche.setSessionId(null);
        appareils.save(fiche);
    }

    private void fermer(String sessionId) {
        try {
            sessions.revoquer(sessionId);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }

    /** Une session déjà close ne doit pas empêcher d'enregistrer l'appareil. */
    private void fermerAncienne(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        try {
            sessions.revoquer(sessionId);
        } catch (RuntimeException ignored) {
            // la nouvelle session reste ouverte
        }
    }

    private static Vue vue(IdentityAppareil appareil, String sessionId) {
        return new Vue(
                appareil.getId(),
                appareil.getAppareilId(),
                appareil.getNom(),
                appareil.getPlateforme().name(),
                appareil.getStatut().name(),
                appareil.getLastSeenAt(),
                courant(appareil, sessionId)
        );
    }

    private static boolean courant(IdentityAppareil appareil, String sessionId) {
        return sessionId != null && sessionId.equals(appareil.getSessionId()) && appareil.getStatut() == Statut.ACTIF;
    }

    private static String appareil(String valeur) {
        String identifiant = requis(valeur, "L'identifiant de l'appareil est requis");
        if (identifiant.length() < 8 || identifiant.length() > 80 || !identifiant.matches("[A-Za-z0-9._-]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'identifiant de l'appareil est invalide");
        }
        return identifiant;
    }

    private static Plateforme plateforme(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La plateforme est requise");
        }
        try {
            return Plateforme.valueOf(valeur.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plateforme inconnue");
        }
    }

    private static String requis(String valeur, String message) {
        if (valeur == null || valeur.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return valeur.trim();
    }

    public record Vue(
            UUID id,
            String appareilId,
            String nom,
            String plateforme,
            String statut,
            Instant vuLe,
            boolean courant
    ) {
    }
}
