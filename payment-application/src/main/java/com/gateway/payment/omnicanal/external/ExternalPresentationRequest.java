package com.gateway.payment.omnicanal.external;

import java.util.List;

/**
 * Corps de {@code POST /api/omnicanalPresentation} attendu par l'omnicanal externe.
 */
public record ExternalPresentationRequest(
        String canal,
        String referenceExterne,
        String codeService,
        String fse,
        String proprieteApi,
        String codeUniteOrganisation,
        List<ExternalParam> param
) {
    public record ExternalParam(String codeParametre, String valeur, String parametreApi) {
    }
}
