package com.gateway.payment.identity.api;

import com.gateway.payment.identity.domain.IdentityChannelUser;
import com.gateway.payment.identity.repository.IdentityChannelUserRepository;
import com.gateway.payment.identity.service.AppareilService;
import com.gateway.payment.identity.service.AppareilService.Vue;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Appareils de confiance de la personne connectée, et liste pour la fiche identité. */
@RestController
@RequestMapping("/api/v1/identity")
public class AppareilController {

    private static final Set<String> STAFF = Set.of(
            "ROLE_ADMIN", "ROLE_CONFIG_MANAGER", "ROLE_OPERATOR", "ROLE_SYSTEM", "ROLE_GROUP_ADMIN"
    );

    private final AppareilService appareils;
    private final IdentityChannelUserRepository utilisateurs;

    public AppareilController(AppareilService appareils, IdentityChannelUserRepository utilisateurs) {
        this.appareils = appareils;
        this.utilisateurs = utilisateurs;
    }

    @GetMapping("/me/appareils")
    @PreAuthorize("isAuthenticated()")
    public List<Vue> lister() {
        IdentityChannelUser utilisateur = utilisateur();
        return appareils.lister(utilisateur.getPartyId(), session());
    }

    @PostMapping("/me/appareils")
    @PreAuthorize("isAuthenticated()")
    public Vue enregistrer(@RequestBody Enregistrer body) {
        return appareils.enregistrer(utilisateur(), session(), body.appareilId(), body.nom(), body.plateforme());
    }

    @PostMapping("/me/appareils/{id}/revoquer")
    @PreAuthorize("isAuthenticated()")
    public void revoquer(@PathVariable UUID id, @RequestBody(required = false) Oubli body) {
        boolean oubli = body != null && body.oubli();
        appareils.revoquer(utilisateur(), id, session(), oubli);
    }

    @GetMapping("/parties/{partyId}/appareils")
    @PreAuthorize("isAuthenticated()")
    public List<Vue> listerParty(@PathVariable UUID partyId) {
        if (!staff()) {
            if (!utilisateur().getPartyId().equals(partyId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à cette party");
            }
        }
        return appareils.listerParty(partyId);
    }

    @PostMapping("/parties/{partyId}/appareils/{id}/revoquer")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    public void revoquerParty(@PathVariable UUID partyId, @PathVariable UUID id) {
        appareils.revoquerPourParty(partyId, id);
    }

    private IdentityChannelUser utilisateur() {
        Authentication auth = authentification();
        List<IdentityChannelUser> trouves = utilisateurs.findByUsernameIgnoreCaseAndEnabledTrue(auth.getName());
        if (trouves.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Aucun accès pour cet identifiant");
        }
        return trouves.getFirst();
    }

    private String session() {
        Object secret = authentification().getCredentials();
        return secret instanceof String valeur && !valeur.isBlank() ? valeur : null;
    }

    private boolean staff() {
        return authentification().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(STAFF::contains);
    }

    private static Authentication authentification() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        return auth;
    }

    public record Enregistrer(String appareilId, String nom, String plateforme) {
    }

    public record Oubli(boolean oubli) {
    }
}
