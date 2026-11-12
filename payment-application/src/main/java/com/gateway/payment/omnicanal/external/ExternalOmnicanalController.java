package com.gateway.payment.omnicanal.external;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Entrée séparée vers l'omnicanal externe.
 * Les routes historiques {@code /api/boundaryByTypeMethode} et {@code /api/omnicanalPresentation}
 * continuent d'exécuter le moteur interne.
 */
@RestController
@RequestMapping("/api/v1/external-omnicanal")
public class ExternalOmnicanalController {

    private final ExternalOmnicanalClient client;

    public ExternalOmnicanalController(ExternalOmnicanalClient client) {
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
