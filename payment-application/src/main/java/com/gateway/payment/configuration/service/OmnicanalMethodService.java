package com.gateway.payment.configuration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.domain.OmnicanalTypeMethode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.api.dto.OmnicanalMethodCreateRequest;
import com.gateway.payment.configuration.api.dto.OmnicanalMethodCreateRequest.OmnicanalParamCreateRequest;
import com.gateway.payment.configuration.api.dto.OmnicanalMethodCreateResponse;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderApi;
import com.gateway.payment.configuration.domain.ProviderEnvironment;
import com.gateway.payment.configuration.domain.ProviderFseApi;
import com.gateway.payment.configuration.domain.ProviderOperation;
import com.gateway.payment.configuration.repository.ProviderApiRepository;
import com.gateway.payment.configuration.repository.ProviderOperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Crée une Méthode Omnicanal rattachée à une Api (ProviderFseApi) :
 * Operation (typeMethode) + ProviderApi endpoint (nomMethode/path + params).
 */
@Service
public class OmnicanalMethodService {

    private final ProviderService providerService;
    private final ProviderFseApiService fseApiService;
    private final ProviderOperationRepository operationRepository;
    private final ProviderApiRepository apiRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public OmnicanalMethodService(
            ProviderService providerService,
            ProviderFseApiService fseApiService,
            ProviderOperationRepository operationRepository,
            ProviderApiRepository apiRepository,
            AuditService auditService,
            ObjectMapper objectMapper
    ) {
        this.providerService = providerService;
        this.fseApiService = fseApiService;
        this.operationRepository = operationRepository;
        this.apiRepository = apiRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<String> listTypeMethodes() {
        java.util.LinkedHashSet<String> codes = new java.util.LinkedHashSet<>();
        Arrays.stream(OmnicanalTypeMethode.values()).map(Enum::name).forEach(codes::add);
        // Opérations gateway courantes (hors enum Omnicanal historique)
        codes.addAll(List.of(
                "LOOKUP_BILL",
                "BILL_LIST",
                "PAY_BILL",
                "GENERATE_OTP",
                "CONFIRM_PAYMENT",
                "GET_STATUS",
                "TRANSFER_EXECUTE",
                "CHECK_STATUS"
        ));
        return codes.stream().sorted().toList();
    }

    @Transactional
    public OmnicanalMethodCreateResponse createMethod(
            UUID providerId,
            OmnicanalMethodCreateRequest request,
            String actor
    ) {
        PaymentProvider provider = providerService.requireProvider(providerId);
        ProviderFseApi fseApi = fseApiService.requireFseApi(providerId, request.fseApiId());

        String typeMethode = request.typeMethode().trim().toUpperCase(Locale.ROOT);
        String httpMethod = normalizeHttp(request.typeAppel());
        String path = normalizePath(request.nomMethode());
        String opCode = typeMethode;
        if (operationRepository.findByProviderIdAndCode(providerId, opCode).isPresent()
                && StringUtils.hasText(request.codeMethode())) {
            opCode = typeMethode + "_" + request.codeMethode().trim();
        }
        if (operationRepository.findByProviderIdAndCode(providerId, opCode).isPresent()) {
            throw new BusinessException("METHOD_EXISTS",
                    "Une méthode/opération existe déjà pour le code " + opCode);
        }

        ProviderEnvironment env = fseApi.getEnvironment();

        String description = "Omnicanal · " + typeMethode
                + (StringUtils.hasText(request.codeMethode()) ? " (" + request.codeMethode().trim() + ")" : "")
                + " · Api " + fseApi.getCode();

        ProviderOperation operation = operationRepository.save(ProviderOperation.builder()
                .provider(provider)
                .code(opCode)
                .nom(path)
                .description(description)
                .idempotent(request.idempotent())
                .actif(request.actif())
                .build());

        String mappingJson = buildRequestMapping(request.parametres());
        String endpointCode = opCode;
        if (apiRepository.findByProviderIdAndCode(providerId, endpointCode).isPresent()) {
            endpointCode = opCode + "_API";
        }

        ProviderApi endpoint = apiRepository.save(ProviderApi.builder()
                .provider(provider)
                .fseApi(fseApi)
                .environment(null)
                .operation(operation)
                .code(endpointCode)
                .nom(path)
                .httpMethod(httpMethod)
                .path(path)
                .contentType("application/json")
                .timeoutSeconds(30)
                .requestMappingJson(mappingJson)
                .responseMappingJson(null)
                .retryEnabled(false)
                .maxAttempts(1)
                .retryDelayMs(2000L)
                .backoffMultiplier(1.0)
                .actif(request.actif())
                .build());

        auditService.record(actor, "OMNI_METHOD_CREATE", "PROVIDER_OPERATION", operation.getId().toString(),
                "Méthode " + typeMethode + " → Api " + fseApi.getCode() + " / " + path);

        return new OmnicanalMethodCreateResponse(
                operation.getId(),
                operation.getCode(),
                endpoint.getId(),
                endpoint.getCode(),
                endpoint.getPath(),
                endpoint.getHttpMethod(),
                fseApi.getId(),
                fseApi.getCode(),
                env != null ? env.getId() : null,
                env != null ? env.getCode() : null,
                mappingJson
        );
    }

    private String buildRequestMapping(List<OmnicanalParamCreateRequest> parametres) {
        if (parametres == null || parametres.isEmpty()) {
            return null;
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (OmnicanalParamCreateRequest p : parametres) {
            if (p == null || !StringUtils.hasText(p.nomExterne()) || !StringUtils.hasText(p.codeParametre())) {
                continue;
            }
            map.put(p.nomExterne().trim(), "${" + p.codeParametre().trim() + "}");
        }
        if (map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception ex) {
            throw new BusinessException("MAPPING_INVALID", "Impossible de sérialiser les paramètres");
        }
    }

    private static String normalizeHttp(String typeAppel) {
        if (typeAppel == null || typeAppel.isBlank() || "NONE".equalsIgnoreCase(typeAppel)) {
            return "POST";
        }
        return typeAppel.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizePath(String nomMethode) {
        if (nomMethode == null || nomMethode.isBlank()) {
            return "/";
        }
        String p = nomMethode.trim();
        return p.startsWith("/") ? p.substring(1) : p;
    }
}
