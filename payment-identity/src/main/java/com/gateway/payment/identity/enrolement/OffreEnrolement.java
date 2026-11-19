package com.gateway.payment.identity.enrolement;

import com.gateway.payment.identity.domain.IdentityPartyProfile;
import com.gateway.payment.identity.domain.IdentityProfile;
import com.gateway.payment.identity.domain.ProfileKind;
import com.gateway.payment.identity.repository.IdentityPartyProfileRepository;
import com.gateway.payment.identity.repository.IdentityProfileRepository;
import com.gateway.payment.identity.service.IdentityService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

/** Affecte le profil service et le profil frais décidés pour le segment retail et le choix du parcours. */
@Component
public class OffreEnrolement {

    static final String SEGMENT = "RETAIL";
    private static final String AUCUN = "AUCUN";

    private final ProfilOffreDecision decision;
    private final IdentityProfileRepository profils;
    private final IdentityPartyProfileRepository affectations;

    public OffreEnrolement(
            ProfilOffreDecision decision,
            IdentityProfileRepository profils,
            IdentityPartyProfileRepository affectations
    ) {
        this.decision = decision;
        this.profils = profils;
        this.affectations = affectations;
    }

    public void appliquer(UUID partyId, String choix) {
        ProfilOffreDecision.Offre offre = decision.offre(SEGMENT, choix);
        affecter(partyId, offre.service(), ProfileKind.SERVICE, "service");
        affecter(partyId, offre.frais(), ProfileKind.TARIFF, "frais");
    }

    private void affecter(UUID partyId, String code, ProfileKind attendu, String libelle) {
        if (code == null || code.isBlank() || AUCUN.equalsIgnoreCase(code)) {
            return;
        }
        IdentityProfile profil = profils.findByTenantIdAndCode(IdentityService.DEFAULT_TENANT, code)
                .orElseThrow(() -> new IllegalArgumentException("Le profil " + libelle + " " + code + " est introuvable"));
        if (profil.getKind() != attendu) {
            throw new IllegalArgumentException("Le profil " + code + " n'est pas un profil " + libelle);
        }
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
    }
}
