package com.gateway.payment.fidelite.api;

import com.gateway.payment.fidelite.domain.Evenement;
import com.gateway.payment.fidelite.service.AttributionService;
import com.gateway.payment.fidelite.service.AttributionService.Accuse;
import com.gateway.payment.fidelite.service.AttributionService.ReglePubliee;
import com.gateway.payment.fidelite.service.RuleEngine.Outcome;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fidelite")
public class AttributionController {

    private final AttributionService attribution;

    public AttributionController(AttributionService attribution) {
        this.attribution = attribution;
    }

    /** Accepté : l'événement est pris en compte. Les points ne sont pas dans la réponse. */
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Accuse recevoir(@RequestBody Evenement event) {
        return attribution.recevoir(tenant(), event);
    }

    /** Calcule les points d'une définition. N'écrit ni règle ni mouvement. */
    @PostMapping("/regles/simuler")
    public Outcome simuler(@RequestBody SimulationRequest request) {
        return attribution.simuler(request.definition(), request.event());
    }

    @PostMapping("/programmes/{id}/regles")
    @ResponseStatus(HttpStatus.CREATED)
    public ReglePubliee publier(@PathVariable UUID id, @RequestBody RegleRequest request) {
        return attribution.publier(tenant(), id, request.code(), request.priority(), request.definition());
    }

    private static String tenant() {
        String tenant = CurrentUser.tenantId();
        return tenant == null ? "CORIS_DEMO" : tenant;
    }

    public record SimulationRequest(String definition, Evenement event) {
    }

    public record RegleRequest(String code, int priority, String definition) {
    }
}
