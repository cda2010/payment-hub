package com.gateway.payment.observability.catalog;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderOperation;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderOperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogue participants FSE — derive des fournisseurs payment-2 (equivalent Omnicanal /api/v1/catalog).
 */
@Service
public class ParticipantCatalogService {

    private final PaymentProviderRepository providerRepository;
    private final ProviderOperationRepository operationRepository;

    public ParticipantCatalogService(
            PaymentProviderRepository providerRepository,
            ProviderOperationRepository operationRepository
    ) {
        this.providerRepository = providerRepository;
        this.operationRepository = operationRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listParticipants() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (PaymentProvider provider : providerRepository.findAllWithServiceTypes()) {
            if (StringUtils.hasText(provider.getCodeFse())) {
                out.add(toParticipant(provider));
            }
        }
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getParticipant(String codeFse) {
        PaymentProvider provider = providerRepository.findByCodeFse(codeFse)
                .flatMap(p -> providerRepository.findByIdWithServiceTypes(p.getId()))
                .orElseThrow(() -> new com.gateway.payment.common.exception.BusinessException(
                        "PARTICIPANT_NOT_FOUND",
                        "Participant FSE " + codeFse + " introuvable",
                        org.springframework.http.HttpStatus.NOT_FOUND
                ));
        return toParticipant(provider);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listCapabilities(String codeFse) {
        PaymentProvider provider = providerRepository.findByCodeFse(codeFse)
                .flatMap(p -> providerRepository.findByIdWithServiceTypes(p.getId()))
                .orElseThrow(() -> new com.gateway.payment.common.exception.BusinessException(
                        "PARTICIPANT_NOT_FOUND",
                        "Participant FSE " + codeFse + " introuvable",
                        org.springframework.http.HttpStatus.NOT_FOUND
                ));
        return buildCapabilities(provider);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> productSupport(String codeFse, String productCode) {
        List<Map<String, Object>> caps = listCapabilities(codeFse);
        boolean supported = caps.stream()
                .anyMatch(c -> productCode.equals(c.get("capabilityCode")));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("participantCode", codeFse);
        out.put("fseCode", codeFse);
        out.put("productCode", productCode);
        out.put("supported", supported);
        out.put("capabilities", caps.stream()
                .filter(c -> productCode.equals(c.get("capabilityCode")))
                .toList());
        return out;
    }

    private Map<String, Object> toParticipant(PaymentProvider provider) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("participantCode", provider.getCodeFse());
        m.put("fseCode", provider.getCodeFse());
        m.put("providerCode", provider.getCode());
        m.put("name", provider.getLibelle());
        m.put("type", "FSE");
        m.put("enabled", provider.isActif());
        m.put("countryCode", provider.getCountryCode());
        m.put("participantKind", provider.getParticipantKind() != null ? provider.getParticipantKind().name() : null);
        m.put("capabilities", buildCapabilities(provider));
        return m;
    }

    private List<Map<String, Object>> buildCapabilities(PaymentProvider provider) {
        List<Map<String, Object>> caps = new ArrayList<>();
        if (provider.getServiceTypes() != null) {
            provider.getServiceTypes().forEach(st -> {
                Map<String, Object> cap = new LinkedHashMap<>();
                cap.put("capabilityCode", st.getCode().name());
                cap.put("label", st.getLibelle());
                cap.put("category", categoryOf(st.getCode()));
                cap.put("enabled", provider.isActif());
                cap.put("operations", operationsFor(provider.getId(), st.getCode()));
                caps.add(cap);
            });
        }
        return caps;
    }

    private List<Map<String, Object>> operationsFor(java.util.UUID providerId, ServiceTypeCode serviceType) {
        return operationRepository.findByProviderId(providerId).stream()
                .filter(ProviderOperation::isActif)
                .map(op -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("operationCode", op.getCode());
                    m.put("nom", op.getNom());
                    m.put("serviceType", serviceType.name());
                    return m;
                })
                .toList();
    }

    private static String categoryOf(ServiceTypeCode code) {
        return switch (code) {
            case TRANSFERT -> "PAYMENT";
            case PAYEMENT, PAIEMENT_FACTURE -> "PAYMENT";
            case CONSULTATION_SOLDE -> "INQUIRY";
            case LINK, UNLINK -> "ACCOUNT";
            default -> throw new IllegalStateException("Unexpected value: " + (code));
        };
    }
}
