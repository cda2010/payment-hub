package com.gateway.payment.catalog.service;

import com.gateway.payment.common.domain.ExecutionMode;
import com.gateway.payment.common.domain.PaymentMethodType;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.domain.FseParticipantKind;
import com.gateway.payment.configuration.domain.PaymentFlow;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ServiceType;
import com.gateway.payment.configuration.repository.PaymentFlowRepository;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ServiceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Crée / aligne l'hôte technique {@code payment_provider} + {@code payment_flow}
 * pour qu'un produit catalogue démarre sans provider client (via {@code workflow_key}).
 */
@Service
public class CatalogHostFlowService {

    private final PaymentProviderRepository providerRepository;
    private final PaymentFlowRepository flowRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    public CatalogHostFlowService(
            PaymentProviderRepository providerRepository,
            PaymentFlowRepository flowRepository,
            ServiceTypeRepository serviceTypeRepository
    ) {
        this.providerRepository = providerRepository;
        this.flowRepository = flowRepository;
        this.serviceTypeRepository = serviceTypeRepository;
    }

    public record HostFlowResult(String providerCode, String workflowKey, ServiceTypeCode serviceType, boolean created) {}

    @Transactional
    public HostFlowResult ensureHostFlow(
            String productCode,
            String workflowKey,
            ServiceTypeCode serviceType,
            Map<String, Object> hostFlowConfig
    ) {
        if (!StringUtils.hasText(workflowKey)) {
            throw new BusinessException("WORKFLOW_KEY_REQUIRED", "workflowKey requis pour créer l'hôte technique");
        }
        Map<String, Object> cfg = hostFlowConfig != null ? hostFlowConfig : Map.of();
        Object auto = cfg.get("autoCreate");
        if (auto == null) {
            auto = cfg.get("auto_create");
        }
        boolean autoCreate = auto == null || Boolean.parseBoolean(String.valueOf(auto));
        if (!autoCreate) {
            return flowRepository.findFirstByBpmProcessKeyAndActifTrue(workflowKey.trim())
                    .map(f -> new HostFlowResult(
                            f.getProvider() != null ? f.getProvider().getCode() : null,
                            workflowKey.trim(),
                            serviceType,
                            false))
                    .orElseThrow(() -> new BusinessException(
                            "HOST_FLOW_MISSING",
                            "Aucun payment_flow actif pour workflow_key=" + workflowKey
                                    + " (hostFlow.autoCreate=false)"));
        }

        String providerCode = first(cfg, "providerCode", "provider_code");
        if (!StringUtils.hasText(providerCode)) {
            providerCode = productCode;
        }
        providerCode = providerCode.trim().toUpperCase(Locale.ROOT);
        ServiceTypeCode st = serviceType != null ? serviceType : ServiceTypeCode.TRANSFERT;

        PaymentProvider provider = providerRepository.findByCodeWithServiceTypes(providerCode).orElse(null);
        boolean created = false;
        if (provider == null) {
            provider = PaymentProvider.builder()
                    .code(providerCode)
                    .libelle(first(cfg, "libelle", "label") != null
                            ? first(cfg, "libelle", "label")
                            : productCode + " (orchestration)")
                    .description("Hôte technique catalogue " + productCode)
                    .actif(true)
                    .ordreAffichage(95)
                    .modeExecution(ExecutionMode.DIRECT)
                    .paymentMethodType(PaymentMethodType.OTHER)
                    .participantKind(parseKind(first(cfg, "participantKind", "participant_kind")))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            provider = providerRepository.save(provider);
            created = true;
        }

        ServiceType serviceTypeEntity = serviceTypeRepository.findById(st)
                .orElseThrow(() -> new BusinessException(
                        "SERVICE_TYPE_NOT_FOUND", "service_type introuvable: " + st));
        if (provider.getServiceTypes() == null || provider.getServiceTypes().stream()
                .noneMatch(s -> s.getCode() == st)) {
            provider.getServiceTypes().add(serviceTypeEntity);
            provider = providerRepository.save(provider);
        }

        PaymentFlow flow = flowRepository.findByProviderIdAndServiceType(provider.getId(), st).orElse(null);
        if (flow == null) {
            flow = PaymentFlow.builder()
                    .provider(provider)
                    .code(st.name())
                    .nom(productCode + " — " + st.name())
                    .serviceType(st)
                    .bpmProcessKey(workflowKey.trim())
                    .description("Flow hôte catalogue " + productCode)
                    .actif(true)
                    .createdAt(Instant.now())
                    .build();
            flowRepository.save(flow);
            created = true;
        } else if (!workflowKey.trim().equals(flow.getBpmProcessKey())) {
            flow.setBpmProcessKey(workflowKey.trim());
            flow.setActif(true);
            flowRepository.save(flow);
        }

        // Aussi exposer un flow trouvé globalement si un autre provider porte déjà la clé
        flowRepository.findFirstByBpmProcessKeyAndActifTrue(workflowKey.trim())
                .orElseThrow(() -> new BusinessException(
                        "HOST_FLOW_ENSURE_FAILED",
                        "Impossible d'assurer payment_flow pour " + workflowKey));

        return new HostFlowResult(providerCode, workflowKey.trim(), st, created);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> extractHostFlowConfig(Map<String, Object> configuration) {
        if (configuration == null) {
            return Map.of();
        }
        Object gateway = configuration.get("gateway");
        if (gateway instanceof Map<?, ?> g) {
            Object hf = g.get("hostFlow");
            if (hf == null) {
                hf = g.get("host_flow");
            }
            if (hf instanceof Map<?, ?> m) {
                return new LinkedHashMap<>((Map<String, Object>) m);
            }
        }
        Object top = configuration.get("hostFlow");
        if (top instanceof Map<?, ?> m) {
            return new LinkedHashMap<>((Map<String, Object>) m);
        }
        return Map.of();
    }

    private static FseParticipantKind parseKind(String raw) {
        if (!StringUtils.hasText(raw)) {
            return FseParticipantKind.RESEAU;
        }
        try {
            return FseParticipantKind.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            return FseParticipantKind.RESEAU;
        }
    }

    private static String first(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object v = map.get(key);
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return String.valueOf(v).trim();
            }
        }
        return null;
    }
}
