package com.gateway.payment.omnicanal.external;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Relais direct, hors parcours.
 * L'enrôlement passe par le moteur {@code /api/v1/boundaryByTypeMethode}.
 */
@RestController
@RequestMapping("/api/v1/external-omnicanal")
public class ExternalOmnicanalController {

    private final AccesOmnicanal client;

    public ExternalOmnicanalController(AccesOmnicanal client) {
        this.client = client;
    }

    @PostMapping("/boundary")
    public Map<String, Object> boundary(
            @RequestParam("codeFSE") String codeFse,
            @RequestParam String codeMethode,
            @RequestParam(required = false) String otrParametre
    ) {
        return client.boundary(codeFse, codeMethode, otrParametre);
    }

    @PostMapping("/presentation")
    public Map<String, Object> presentation(@RequestBody ExternalPresentationRequest request) {
        return client.presentation(request);
    }
}
