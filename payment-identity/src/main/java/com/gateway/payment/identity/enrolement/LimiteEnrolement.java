package com.gateway.payment.identity.enrolement;

import com.gateway.payment.identity.domain.IdentityPartyProfile;
import com.gateway.payment.identity.domain.IdentityProfile;
import com.gateway.payment.identity.repository.IdentityPartyProfileRepository;
import com.gateway.payment.identity.repository.IdentityProfileRepository;
import com.gateway.payment.identity.service.IdentityService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

/** Affecte le profil de limite décidé pour le segment retail et le choix du parcours. */
@Component
public class LimiteEnrolement {

    static final String SEGMENT = "RETAIL";

    private final ProfilLimiteDecision decision;
    private final IdentityProfileRepository profils;
    private final IdentityPartyProfileRepository affectations;

    public LimiteEnrolement(
            ProfilLimiteDecision decision,
            IdentityProfileRepository profils,
            IdentityPartyProfileRepository affectations
    ) {
        this.decision = decision;
        this.profils = profils;
        this.affectations = affectations;
    }

    public String appliquer(UUID partyId, String choix) {
        String code = decision.profil(SEGMENT, choix);
        IdentityProfile profil = profils.findByTenantIdAndCode(IdentityService.DEFAULT_TENANT, code)
                .orElseThrow(() -> new IllegalArgumentException("Le profil de limite " + code + " est introuvable"));
        affectations.findByPartyIdAndProfileId(partyId, profil.getId()).ifPresentOrElse(affectation -> {
            affectation.setActive(true);
            if (affectation.getDateDebut() == null) {
                affectation.setDateDebut(LocalDate.now());
            }
        }, () -> affectations.save(IdentityPartyProfile.builder()
                .partyId(partyId)
                .profileId(profil.getId())
                .dateDebut(LocalDate.now())
                .active(true)
                .build()));
        return code;
    }
}
