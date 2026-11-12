package com.gateway.payment.network.service;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.api.dto.NetworkDtos.PositionDto;
import com.gateway.payment.network.api.dto.NetworkDtos.UniteResume;
import com.gateway.payment.network.domain.TypeUnite;
import com.gateway.payment.network.domain.UniteOrganisation;
import com.gateway.payment.network.domain.Zone;
import com.gateway.payment.network.repository.UniteOrganisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Position d'une unité, d'un pays (code ISO) ou d'une zone : tout ce qui la couvre, du plus précis au moins
 * précis (l'unité, ses ascendants jusqu'au pays, les regroupements qui la couvrent, la zone).
 */
@Service
@Transactional(readOnly = true)
public class PositionService {

    private final UniteOrganisationRepository unites;
    private final RegroupementService regroupements;
    private final ZoneService zones;

    public PositionService(UniteOrganisationRepository unites, RegroupementService regroupements, ZoneService zones) {
        this.unites = unites;
        this.regroupements = regroupements;
        this.zones = zones;
    }

    /** Exactement un des critères : id unité, code unité, pays (ISO) ou zone. */
    public PositionDto position(UUID uniteId, String pays, String zone, String codeUnite) {
        long renseignes = Stream.of(
                        uniteId,
                        vide(pays) ? null : pays,
                        vide(zone) ? null : zone,
                        vide(codeUnite) ? null : codeUnite)
                .filter(v -> v != null).count();
        if (renseignes != 1) {
            throw NetworkException.regle("POSITION_INVALIDE",
                    "Indiquez exactement un critère : une unité (id ou code), un pays (code ISO) ou une zone.");
        }
        if (uniteId != null) {
            return position(unites.findById(uniteId).orElseThrow(() ->
                    NetworkException.introuvable("UNITE_INEXISTANTE", "Unité " + uniteId + " introuvable.")));
        }
        if (!vide(codeUnite)) {
            String code = codeUnite.trim().toUpperCase(Locale.ROOT);
            return position(unites.findByCode(code).orElseThrow(() ->
                    NetworkException.introuvable("UNITE_INEXISTANTE", "Unité " + code + " introuvable.")));
        }
        if (!vide(pays)) {
            String code = pays.trim().toUpperCase(Locale.ROOT);
            return position(unites.findByCodeAndType(code, TypeUnite.PAYS).orElseThrow(() ->
                    NetworkException.introuvable("PAYS_INEXISTANT", "Pays " + code + " introuvable.")));
        }
        Zone z = zones.charger(zone);
        return new PositionDto(z.getCode(), z.getDevise(), null, List.of(), List.of());
    }

    /** Compat : id / pays / zone uniquement. */
    public PositionDto position(UUID uniteId, String pays, String zone) {
        return position(uniteId, pays, zone, null);
    }

    private PositionDto position(UniteOrganisation u) {
        Zone z = zones.charger(u.getZone());
        List<UniteResume> lignee = new ArrayList<>();
        lignee.add(UniteResume.from(u));
        unites.findAllById(u.idsAscendants()).stream()
                .sorted(Comparator.comparingInt(UniteOrganisation::getNiveau).reversed())
                .map(UniteResume::from)
                .forEach(lignee::add);
        return new PositionDto(z.getCode(), z.getDevise(), UniteResume.from(u), lignee,
                regroupements.couvertures(u.getId()));
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
