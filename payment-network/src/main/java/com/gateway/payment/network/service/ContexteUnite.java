package com.gateway.payment.network.service;

import com.gateway.payment.network.NetworkException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/** Unité choisie pour la session. L'en-tête est absent quand la personne n'a qu'une unité. */
@Component
public class ContexteUnite {

    public static final String ENTETE = "X-Unite-Active";

    public UUID demandee() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servlet)) {
            return null;
        }
        String brut = servlet.getRequest().getHeader(ENTETE);
        if (brut == null || brut.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(brut.trim());
        } catch (IllegalArgumentException e) {
            throw NetworkException.regle("UNITE_ACTIVE_INVALIDE", "L'unité active n'est pas un identifiant.");
        }
    }
}
