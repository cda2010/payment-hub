package com.gateway.payment.network.service;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.api.dto.NetworkDtos.CreationZoneRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.ModificationZoneRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.ZoneDto;
import com.gateway.payment.network.domain.StatutUnite;
import com.gateway.payment.network.domain.TypeUnite;
import com.gateway.payment.network.domain.Zone;
import com.gateway.payment.network.repository.UniteOrganisationRepository;
import com.gateway.payment.network.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Référentiel des zones. Une zone se crée, se modifie et se désactive, mais ne se supprime pas : ses unités la
 * référencent par son code, qui ne change jamais.
 */
@Service
@Transactional
public class ZoneService {

    private final ZoneRepository zones;
    private final UniteOrganisationRepository unites;

    public ZoneService(ZoneRepository zones, UniteOrganisationRepository unites) {
        this.zones = zones;
        this.unites = unites;
    }

    @Transactional(readOnly = true)
    public List<ZoneDto> lister() {
        return zones.findAllByOrderByCode().stream().map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public ZoneDto consulter(String code) {
        return dto(charger(code));
    }

    public ZoneDto creer(CreationZoneRequest req, String acteur) {
        String code = normaliser(req.code());
        if (zones.existsById(code)) {
            throw NetworkException.conflit("ZONE_EXISTANTE", "La zone " + code + " existe déjà.");
        }
        Zone zone = Zone.builder()
                .code(code)
                .libelle(req.libelle().trim())
                .devise(normaliser(req.devise()))
                .statut(StatutUnite.ACTIF)
                .createdBy(acteur)
                .updatedBy(acteur)
                .build();
        zones.save(zone);
        zones.flush();
        return dto(zone);
    }

    public ZoneDto modifier(String code, ModificationZoneRequest req, String acteur) {
        Zone zone = charger(code);
        String devise = normaliser(req.devise());
        if (!devise.equals(zone.getDevise()) && unites.countByZone(zone.getCode()) > 0) {
            throw NetworkException.regle("DEVISE_FIGEE", "La zone " + zone.getCode() + " contient déjà des unités : "
                    + "sa devise (" + zone.getDevise() + ") ne change plus.");
        }
        zone.setLibelle(req.libelle().trim());
        zone.setDevise(devise);
        zone.setUpdatedBy(acteur);
        zones.flush();
        return dto(zone);
    }

    public ZoneDto desactiver(String code, String acteur) {
        Zone zone = charger(code);
        if (!zone.estActive()) {
            return dto(zone);
        }
        if (unites.existsByZoneAndTypeAndStatut(zone.getCode(), TypeUnite.PAYS, StatutUnite.ACTIF)) {
            throw NetworkException.regle("ZONE_AVEC_PAYS_ACTIFS", "La zone " + zone.getCode()
                    + " a encore des pays actifs : désactivez-les d'abord.");
        }
        zone.setStatut(StatutUnite.INACTIF);
        zone.setUpdatedBy(acteur);
        zones.flush();
        return dto(zone);
    }

    public ZoneDto activer(String code, String acteur) {
        Zone zone = charger(code);
        if (!zone.estActive()) {
            zone.setStatut(StatutUnite.ACTIF);
            zone.setUpdatedBy(acteur);
            zones.flush();
        }
        return dto(zone);
    }

    /** Zone existante et active, pour y créer un pays ou un regroupement, ou y réactiver un pays. */
    @Transactional(readOnly = true)
    public Zone exigerActive(String code) {
        Zone zone = charger(code);
        if (!zone.estActive()) {
            throw NetworkException.regle("ZONE_INACTIVE", "La zone " + zone.getCode() + " est inactive.");
        }
        return zone;
    }

    @Transactional(readOnly = true)
    public Zone charger(String code) {
        String normalise = code == null ? "" : normaliser(code);
        return zones.findById(normalise).orElseThrow(() ->
                NetworkException.introuvable("ZONE_INEXISTANTE", "Zone " + normalise + " introuvable."));
    }

    static String normaliser(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private ZoneDto dto(Zone z) {
        return new ZoneDto(z.getCode(), z.getLibelle(), z.getDevise(), z.getStatut(),
                unites.countByZoneAndType(z.getCode(), TypeUnite.PAYS), unites.countByZone(z.getCode()),
                z.getCreatedAt(), z.getCreatedBy(), z.getUpdatedAt(), z.getUpdatedBy());
    }
}
