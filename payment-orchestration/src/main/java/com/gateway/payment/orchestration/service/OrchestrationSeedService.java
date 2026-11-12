package com.gateway.payment.orchestration.service;

import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderApi;
import com.gateway.payment.configuration.domain.ProviderOperation;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderApiRepository;
import com.gateway.payment.configuration.repository.ProviderOperationRepository;
import com.gateway.payment.orchestration.domain.InterfaceUtilisateur;
import com.gateway.payment.orchestration.domain.TypeEnvoi;
import com.gateway.payment.orchestration.repository.InterfaceUtilisateurRepository;
import com.gateway.payment.orchestration.repository.ParametreUtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Seed des interfaces utilisateur (+ ParametreUtilisateur) à partir des opérations/endpoints.
 * <p>
 * Génération bottom-up : une interface REQUEST par opération active du provider
 * ({@code typeMethode = operation.code}), avec paramètres défaut si le catalogue les connaît.
 */
@Service
public class OrchestrationSeedService {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationSeedService.class);

    /**
     * Templates Omnicanal-like : codeParametre → codeParametreMethode / extracteur
     * ({@link CompteFieldExtractors}, ex. {@code NUMERO_COMPTE} — plus de réflexion getXxx).
     */
    private final InterfaceParamTemplateCatalog paramTemplateCatalog;

    private final PaymentProviderRepository providerRepository;
    private final ProviderOperationRepository operationRepository;
    private final ProviderApiRepository apiRepository;
    private final InterfaceUtilisateurRepository interfaceRepository;
    private final ParametreUtilisateurRepository parametreRepository;

    public OrchestrationSeedService(
            PaymentProviderRepository providerRepository,
            ProviderOperationRepository operationRepository,
            ProviderApiRepository apiRepository,
            InterfaceUtilisateurRepository interfaceRepository,
            ParametreUtilisateurRepository parametreRepository,
            InterfaceParamTemplateCatalog paramTemplateCatalog
    ) {
        this.providerRepository = providerRepository;
        this.operationRepository = operationRepository;
        this.apiRepository = apiRepository;
        this.interfaceRepository = interfaceRepository;
        this.parametreRepository = parametreRepository;
        this.paramTemplateCatalog = paramTemplateCatalog;
    }

    @Transactional
    public List<String> seedInterfacesForProvider(UUID providerId) {
        return seedInterfacesForProvider(requireProvider(providerId));
    }

    public PaymentProvider requireProvider(UUID providerId) {
        return providerRepository.findById(providerId)
                .orElseThrow(() -> new com.gateway.payment.common.exception.BusinessException(
                        "PROVIDER_NOT_FOUND", "Fournisseur introuvable", org.springframework.http.HttpStatus.NOT_FOUND));
    }

    @Transactional
    public List<String> seedInterfacesForProvider(PaymentProvider provider) {
        if (!StringUtils.hasText(provider.getCodeFse())) {
            return List.of();
        }

        List<ProviderOperation> operations = operationRepository.findByProviderId(provider.getId()).stream()
                .filter(ProviderOperation::isActif)
                .sorted(Comparator.comparing(ProviderOperation::getCode, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Set<String> upserted = new LinkedHashSet<>();
        for (ProviderOperation operation : operations) {
            String typeMethode = normalize(operation.getCode());
            if (!StringUtils.hasText(typeMethode)) {
                continue;
            }
            upsertInterface(provider, operation, typeMethode);
            upserted.add(typeMethode);
        }
        return new ArrayList<>(upserted);
    }

    private void upsertInterface(PaymentProvider provider, ProviderOperation operation, String typeMethode) {
        ProviderApi endpoint = apiRepository.findByOperationId(operation.getId())
                .stream()
                .filter(ProviderApi::isActif)
                .findFirst()
                .orElse(null);

        InterfaceUtilisateur entity = interfaceRepository
                .findByCodeFseAndServiceTypeAndTypeMethodeAndTypeEnvoi(
                        provider.getCodeFse(),
                        paramTemplateCatalog.serviceTypeFor(typeMethode),
                        typeMethode,
                        TypeEnvoi.REQUEST
                )
                .or(() -> findLegacyByTypeMethode(provider, typeMethode))
                .orElseGet(() -> InterfaceUtilisateur.builder()
                        .provider(provider)
                        .codeFse(provider.getCodeFse())
                        .serviceType(paramTemplateCatalog.serviceTypeFor(typeMethode))
                        .typeMethode(typeMethode)
                        .typeEnvoi(TypeEnvoi.REQUEST)
                        .build());

        entity.setProvider(provider);
        entity.setCodeFse(provider.getCodeFse());
        entity.setServiceType(paramTemplateCatalog.serviceTypeFor(typeMethode));
        entity.setTypeMethode(typeMethode);
        entity.setTypeEnvoi(TypeEnvoi.REQUEST);
        entity.setProviderOperation(operation);
        entity.setProviderApi(endpoint);
        entity.setNomInterface(provider.getLibelle() + " / " + typeMethode);
        entity.setCodeInterface(typeMethode);
        entity.setActif(true);
        InterfaceUtilisateur saved = interfaceRepository.save(entity);
        paramTemplateCatalog.seedDefaultParametres(saved, parametreRepository);
        log.info("Interface utilisateur upserted: FSE {} / {}", provider.getCodeFse(), typeMethode);
    }

    /**
     * Si une interface existait déjà avec un autre serviceType pour le même typeMethode REQUEST,
     * on la réutilise pour éviter les doublons lors du basculement catalogue → opérations.
     */
    private Optional<InterfaceUtilisateur> findLegacyByTypeMethode(PaymentProvider provider, String typeMethode) {
        return interfaceRepository.findByProviderIdOrderByTypeMethodeAsc(provider.getId()).stream()
                .filter(i -> typeMethode.equalsIgnoreCase(i.getTypeMethode()))
                .filter(i -> i.getTypeEnvoi() == TypeEnvoi.REQUEST)
                .findFirst();
    }

    @Transactional
    public int seedAllKnownProviders() {
        int count = 0;
        for (PaymentProvider provider : providerRepository.findByCodeFseIsNotNull()) {
            if (!provider.getCodeFse().isBlank()) {
                seedInterfacesForProvider(provider);
                count++;
            }
        }
        return count;
    }

    private static String normalize(String typeMethode) {
        return InterfaceParamTemplateCatalog.normalize(typeMethode);
    }
}
