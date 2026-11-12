package com.gateway.payment.omnicanal.external;

import com.gateway.payment.common.domain.ExecutionMode;
import com.gateway.payment.common.domain.PaymentMethodType;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.api.dto.ProviderResponse;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Fournisseur local s'il existe encore, sinon la fiche omnicanal correspondante.
 * La fiche omnicanal n'est pas recopiée dans {@code payment_provider}.
 */
@Service
public class RuntimeProviderResolver {

    private final PaymentProviderRepository providers;
    private final OmnicanalProviderCatalog omnicanal;

    public RuntimeProviderResolver(PaymentProviderRepository providers, OmnicanalProviderCatalog omnicanal) {
        this.providers = providers;
        this.omnicanal = omnicanal;
    }

    public PaymentProvider require(String code) {
        return find(code).orElseThrow(() -> new BusinessException(
                "PROVIDER_NOT_FOUND",
                "Fournisseur introuvable: " + code,
                HttpStatus.NOT_FOUND));
    }

    public Optional<PaymentProvider> find(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String trimmed = code.trim();
        Optional<PaymentProvider> local = providers.findByCodeWithServiceTypes(trimmed);
        if (local.isPresent()) {
            return local;
        }
        return remote(trimmed).map(this::detach);
    }

    /**
     * Code FSE du fournisseur : la fiche locale si elle le porte, sinon l'omnicanal
     * (code FSE exact, code externe, ou libellé de rail tel que {@code CORIS_MONEY}).
     */
    public Optional<String> codeFse(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String trimmed = code.trim();
        Optional<PaymentProvider> local = providers.findByCode(trimmed);
        if (local.isPresent() && local.get().getCodeFse() != null && !local.get().getCodeFse().isBlank()) {
            return Optional.of(local.get().getCodeFse().trim());
        }
        return remote(trimmed)
                .map(ProviderResponse::codeFse)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim);
    }

    private Optional<ProviderResponse> remote(String code) {
        List<ProviderResponse> remote = omnicanal.providers();
        if (remote == null || remote.isEmpty()) {
            return Optional.empty();
        }
        return OmnicanalProviderMatch.best(code, remote);
    }

    private PaymentProvider detach(ProviderResponse remote) {
        PaymentProvider provider = new PaymentProvider();
        provider.setId(remote.id());
        provider.setCode(remote.codeFse() != null ? remote.codeFse() : remote.code());
        provider.setLibelle(remote.libelle() != null ? remote.libelle() : provider.getCode());
        provider.setActif(remote.actif());
        provider.setPaymentMethodType(remote.paymentMethodType() != null
                ? remote.paymentMethodType() : PaymentMethodType.OTHER);
        provider.setModeExecution(remote.modeExecution() != null
                ? remote.modeExecution() : ExecutionMode.DIRECT);
        provider.setCodeFse(remote.codeFse());
        provider.setCodeExterne(remote.codeExterne());
        provider.setGroupCode(remote.groupCode());
        provider.setCountryCode(remote.countryCode());
        provider.setParticipantKind(remote.participantKind());
        provider.setFseStatut(remote.fseStatut());
        provider.setServiceTypes(new HashSet<>());
        return provider;
    }
}
