package com.gateway.payment.orchestration.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderApi;
import com.gateway.payment.configuration.domain.ProviderOperation;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderApiRepository;
import com.gateway.payment.configuration.repository.ProviderOperationRepository;
import com.gateway.payment.orchestration.domain.InterfaceUtilisateur;
import com.gateway.payment.orchestration.domain.ParametreUtilisateur;
import com.gateway.payment.orchestration.domain.ProprieteApi;
import com.gateway.payment.orchestration.domain.TypeEnvoi;
import com.gateway.payment.orchestration.repository.InterfaceUtilisateurRepository;
import com.gateway.payment.orchestration.repository.ParametreUtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Importe InterfaceUtilisateur + ParametreUtilisateur depuis un export Omnicanal (JSON).
 */
@Service
public class OmnicanalInterfaceImportService {

    private static final Logger log = LoggerFactory.getLogger(OmnicanalInterfaceImportService.class);
    public static final String SONABEL_FIXTURE = "omnicanal-migration/interfaces/sonabel.json";

    private final ObjectMapper objectMapper;
    private final PaymentProviderRepository providerRepository;
    private final ProviderOperationRepository operationRepository;
    private final ProviderApiRepository apiRepository;
    private final InterfaceUtilisateurRepository interfaceRepository;
    private final ParametreUtilisateurRepository parametreRepository;
    private final InterfaceParamTemplateCatalog paramTemplateCatalog;
    private final OrchestrationInterfaceCache interfaceCache;

    public OmnicanalInterfaceImportService(
            ObjectMapper objectMapper,
            PaymentProviderRepository providerRepository,
            ProviderOperationRepository operationRepository,
            ProviderApiRepository apiRepository,
            InterfaceUtilisateurRepository interfaceRepository,
            ParametreUtilisateurRepository parametreRepository,
            InterfaceParamTemplateCatalog paramTemplateCatalog,
            OrchestrationInterfaceCache interfaceCache
    ) {
        this.objectMapper = objectMapper;
        this.providerRepository = providerRepository;
        this.operationRepository = operationRepository;
        this.apiRepository = apiRepository;
        this.interfaceRepository = interfaceRepository;
        this.parametreRepository = parametreRepository;
        this.paramTemplateCatalog = paramTemplateCatalog;
        this.interfaceCache = interfaceCache;
    }

    @Transactional
    public ImportResult importSonabel() {
        return importClasspath(SONABEL_FIXTURE);
    }

    @Transactional
    public ImportResult importClasspath(String classpath) {
        Fixture fixture = load(classpath);
        int interfaces = 0;
        int params = 0;
        List<String> details = new ArrayList<>();
        if (fixture.interfaces() == null) {
            return new ImportResult(0, 0, List.of());
        }
        for (IfaceSpec spec : fixture.interfaces()) {
            if (spec == null || !StringUtils.hasText(spec.codeFse()) || !StringUtils.hasText(spec.typeMethode())) {
                continue;
            }
            Optional<PaymentProvider> providerOpt = providerRepository.findByCodeFse(spec.codeFse().trim());
            if (providerOpt.isEmpty()) {
                details.add("SKIP " + spec.codeFse() + "/" + spec.typeMethode() + " (provider absent)");
                continue;
            }
            PaymentProvider provider = providerOpt.get();
            String typeMethode = spec.typeMethode().trim().toUpperCase(Locale.ROOT);
            if (isResponseEnvoi(spec.typeEnvoi())) {
                String skip = "SKIP " + spec.codeFse() + "/" + typeMethode + " (RESPONSE — BillResponse contract)";
                details.add(skip);
                log.warn("IU RESPONSE ignorée à l'import (contrat BillResponse fixe): {} / {}",
                        spec.codeFse(), typeMethode);
                continue;
            }
            TypeEnvoi typeEnvoi = parseEnvoi(spec.typeEnvoi());
            ServiceTypeCode serviceType = paramTemplateCatalog.serviceTypeFor(typeMethode);

            ProviderOperation operation = operationRepository
                    .findByProviderIdAndCode(provider.getId(), typeMethode)
                    .orElse(null);
            ProviderApi api = null;
            if (operation != null) {
                api = apiRepository.findByOperationId(operation.getId()).stream()
                        .filter(ProviderApi::isActif)
                        .findFirst()
                        .orElse(null);
            }

            InterfaceUtilisateur iface = interfaceRepository
                    .findByCodeFseAndServiceTypeAndTypeMethodeAndTypeEnvoi(
                            provider.getCodeFse(), serviceType, typeMethode, typeEnvoi)
                    .or(() -> interfaceRepository.findByProviderIdOrderByTypeMethodeAsc(provider.getId()).stream()
                            .filter(i -> typeMethode.equalsIgnoreCase(i.getTypeMethode())
                                    && i.getTypeEnvoi() == typeEnvoi)
                            .findFirst())
                    .orElseGet(() -> InterfaceUtilisateur.builder()
                            .provider(provider)
                            .codeFse(provider.getCodeFse())
                            .serviceType(serviceType)
                            .typeMethode(typeMethode)
                            .typeEnvoi(typeEnvoi)
                            .build());

            iface.setProvider(provider);
            iface.setCodeFse(provider.getCodeFse());
            iface.setServiceType(serviceType);
            iface.setTypeMethode(typeMethode);
            iface.setTypeEnvoi(typeEnvoi);
            iface.setProviderOperation(operation);
            iface.setProviderApi(api);
            iface.setCodeInterface(StringUtils.hasText(spec.codeInterface())
                    ? spec.codeInterface().trim()
                    : typeMethode);
            iface.setNomInterface(StringUtils.hasText(spec.nomInterface())
                    ? spec.nomInterface().trim()
                    : provider.getLibelle() + " / " + typeMethode);
            iface.setActif(true);
            InterfaceUtilisateur saved = interfaceRepository.save(iface);

            // Remplace les constitutés par ceux d'Omnicanal
            List<ParametreUtilisateur> existing =
                    parametreRepository.findByInterfaceUtilisateurIdOrderByOrdreAscCodeParametreAsc(saved.getId());
            for (ParametreUtilisateur old : existing) {
                parametreRepository.delete(old);
            }

            int ordre = 0;
            int added = 0;
            if (spec.parametres() != null) {
                for (ParamSpec p : spec.parametres()) {
                    if (p == null || !StringUtils.hasText(p.codeParametre())) {
                        continue;
                    }
                    ordre++;
                    parametreRepository.save(ParametreUtilisateur.builder()
                            .interfaceUtilisateur(saved)
                            .nomParametre(blankToNull(p.nomParametre()))
                            .codeParametre(p.codeParametre().trim())
                            .codeParametreMethode(blankToNull(p.codeParametreMethode()))
                            .typeParametre(StringUtils.hasText(p.typeParametre()) ? p.typeParametre() : "STRING")
                            .valeur(blankToNull(p.valeur()))
                            .parametreApi(parseProprieteApi(p.parametreApi()))
                            .attribut(blankToNull(p.attribut()))
                            .ordre(p.ordre() != null ? p.ordre() : ordre)
                            .affiche(true)
                            .build());
                    added++;
                }
            }
            interfaceCache.invalidate(saved.getCodeFse(), saved.getTypeMethode(), saved.getTypeEnvoi());
            interfaces++;
            params += added;
            details.add(saved.getCodeFse() + "/" + typeMethode + "/" + typeEnvoi + " → " + added + " PU");
            log.info("IU Omnicanal importée {} / {} ({} params)", saved.getCodeFse(), typeMethode, added);
        }
        return new ImportResult(interfaces, params, details);
    }

    private Fixture load(String classpath) {
        try (InputStream in = new ClassPathResource(classpath).getInputStream()) {
            return objectMapper.readValue(in, Fixture.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Lecture fixture " + classpath + ": " + ex.getMessage(), ex);
        }
    }

    private static boolean isResponseEnvoi(String raw) {
        return StringUtils.hasText(raw) && "RESPONSE".equalsIgnoreCase(raw.trim());
    }

    private static TypeEnvoi parseEnvoi(String raw) {
        if (!StringUtils.hasText(raw) || isResponseEnvoi(raw)) {
            return TypeEnvoi.REQUEST;
        }
        try {
            return TypeEnvoi.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return TypeEnvoi.REQUEST;
        }
    }

    private static ProprieteApi parseProprieteApi(String raw) {
        if (!StringUtils.hasText(raw) || "EMPTY".equalsIgnoreCase(raw.trim())) {
            return null;
        }
        try {
            return ProprieteApi.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String blankToNull(String v) {
        return StringUtils.hasText(v) ? v.trim() : null;
    }

    public record ImportResult(int interfacesUpserted, int parametresUpserted, List<String> details) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fixture(Integer version, String source, List<IfaceSpec> interfaces) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IfaceSpec(
            String codeFse,
            String codeInterface,
            String nomInterface,
            String typeEnvoi,
            String typeMethode,
            List<ParamSpec> parametres
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ParamSpec(
            String nomParametre,
            String codeParametre,
            String typeParametre,
            String valeur,
            String parametreApi,
            String codeParametreMethode,
            String attribut,
            Integer ordre
    ) {}
}
