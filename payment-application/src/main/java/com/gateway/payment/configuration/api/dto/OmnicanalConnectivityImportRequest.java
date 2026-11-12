package com.gateway.payment.configuration.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Bundle de connectivité Omnicanal (Api + Methodes + ParametreMethode)
 * à appliquer sur un {@code PaymentProvider} payment-2.
 */
public record OmnicanalConnectivityImportRequest(
        Integer version,
        String source,
        String codeFse,
        String providerCodeHint,
        /** Environnement cible (défaut DEVELOPPEMENT) — ne touche pas SANDBOX actif. */
        String targetEnvironmentCode,
        @NotNull @Valid OmnicanalApiSpec api,
        @NotNull List<@Valid OmnicanalMethodSpec> methods,
        /** Métadonnées export DB Omnicanal (création PaymentProvider). */
        String providerLibelle,
        String participantKindHint,
        String fseStatut,
        String groupCode,
        String codeExterne,
        String typeEtc
) {
    public record OmnicanalApiSpec(
            String nomApi,
            String codeApi,
            String type,
            String host,
            String port,
            Boolean useSsl,
            Boolean useProxy,
            @NotBlank String baseUrl,
            String authType,
            String clientId,
            String clientSecret,
            String tokenUrl,
            String grantType,
            String username,
            String password
    ) {}

    public record OmnicanalMethodSpec(
            String typeMethode,
            String codeMethode,
            @NotBlank String operationCode,
            String nom,
            @NotBlank String httpMethod,
            @NotBlank String path,
            String typeReponse,
            String requestMappingJson,
            List<OmnicanalParamSpec> parameters
    ) {}

    public record OmnicanalParamSpec(
            String code,
            String nomExterne,
            String type,
            String categorie
    ) {}
}
