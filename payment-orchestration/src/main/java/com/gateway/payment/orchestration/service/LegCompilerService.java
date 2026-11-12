package com.gateway.payment.orchestration.service;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.omnicanal.domain.CasExecution;
import com.gateway.payment.orchestration.api.dto.LegCompileRequest;
import com.gateway.payment.orchestration.api.dto.LegCompileResponse;
import com.gateway.payment.orchestration.domain.CompteFse;
import com.gateway.payment.orchestration.domain.InterfaceUtilisateur;
import com.gateway.payment.orchestration.domain.LigneSchemaEcriture;
import com.gateway.payment.orchestration.domain.ParametreSchema;
import com.gateway.payment.orchestration.domain.ParametreUtilisateur;
import com.gateway.payment.orchestration.domain.RelationFse;
import com.gateway.payment.orchestration.domain.TypeEnvoi;
import com.gateway.payment.orchestration.repository.CompteFseRepository;
import com.gateway.payment.orchestration.repository.LigneSchemaEcritureRepository;
import com.gateway.payment.orchestration.repository.RelationFseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Compilation d'une jambe FSE :
 * {@link InterfaceUtilisateur} (+ {@link ParametreUtilisateur})
 * → {@link ParametreSchema} (+ catalogue {@code compte_fse} / {@code relation_fse})
 * → map de params résolus.
 * <p>
 * Dérivation de champs compte via {@link CompteFieldExtractor} (codes typés, pas de réflexion).
 */
@Service
public class LegCompilerService {

    private final InterfaceUtilisateurService interfaceService;
    private final LigneSchemaEcritureRepository ligneRepository;
    private final RelationFseRepository relationRepository;
    private final CompteFseRepository compteRepository;
    private final CompteFieldExtractorRegistry extractorRegistry;
    private final ParametrePayloadExtractor payloadExtractor;

    public LegCompilerService(
            InterfaceUtilisateurService interfaceService,
            LigneSchemaEcritureRepository ligneRepository,
            RelationFseRepository relationRepository,
            CompteFseRepository compteRepository,
            CompteFieldExtractorRegistry extractorRegistry,
            ParametrePayloadExtractor payloadExtractor
    ) {
        this.interfaceService = interfaceService;
        this.ligneRepository = ligneRepository;
        this.relationRepository = relationRepository;
        this.compteRepository = compteRepository;
        this.extractorRegistry = extractorRegistry != null
                ? extractorRegistry
                : CompteFieldExtractorRegistry.defaults();
        this.payloadExtractor = payloadExtractor != null
                ? payloadExtractor
                : new ParametrePayloadExtractor();
    }

    @Transactional(readOnly = true)
    public LegCompileResponse compile(LegCompileRequest request) {
        String codeFse = interfaceService.resolveCodeFse(
                request.sourceCodeFse(),
                request.destCodeFse(),
                request.apiAUtiliser()
        );
        InterfaceUtilisateur iface = interfaceService.resolveForLeg(
                codeFse,
                request.typeMethode(),
                TypeEnvoi.REQUEST
        );

        Map<String, String> userParams = request.userParams() != null
                ? new HashMap<>(request.userParams())
                : new HashMap<>();

        // 0) Extractions sous-valeurs payload (REGEX défaut, Groovy si extractScript)
        payloadExtractor.apply(iface.getParametres(), userParams);

        Map<String, CompteFieldExtractor> extractors = extractorsByMethodRole(iface.getParametres());

        // 1) Binding constitues InterfaceUtilisateur
        Map<String, String> resolved = bindInterfaceParams(iface.getParametres(), userParams);

        // 2) Redirections ParametreSchema + lookup compte (équivalent rechercheParamNonAssigne)
        if (request.ligneSchemaEcritureId() != null) {
            LigneSchemaEcriture ligne = ligneRepository.findByIdWithParametres(request.ligneSchemaEcritureId())
                    .orElseGet(() -> ligneRepository.findById(request.ligneSchemaEcritureId()).orElse(null));
            if (ligne != null && ligne.getParametres() != null && !ligne.getParametres().isEmpty()) {
                ServiceTypeCode serviceType = resolveServiceType(ligne, request.typeMethode());
                applySchemaBindings(
                        resolved, userParams, ligne.getParametres(), request, codeFse, serviceType, extractors
                );
            }
        }

        // 3) Re-propager rôle méthode → clé API d'interface (après éventuelle résolution LORO)
        propagateInterfaceAliases(iface.getParametres(), resolved);

        // 4) Compléter avec les userParams restants (hors binding)
        userParams.forEach((k, v) -> resolved.putIfAbsent(k, v != null ? v : ""));

        CasExecution cas = request.cas() != null ? request.cas() : CasExecution.NORMAL;
        Map<String, String> finalResolved = cas.isReversion() ? LegParamReverser.reverse(resolved) : resolved;

        return new LegCompileResponse(
                codeFse,
                request.typeMethode().trim().toUpperCase(),
                request.apiAUtiliser(),
                iface.getProvider().getId(),
                iface.getProviderOperation() != null ? iface.getProviderOperation().getId() : null,
                iface.getProviderOperation() != null ? iface.getProviderOperation().getCode() : null,
                iface.getProviderApi() != null ? iface.getProviderApi().getId() : null,
                iface.getProviderApi() != null ? iface.getProviderApi().getPath() : null,
                iface.getProviderApi() != null ? iface.getProviderApi().getHttpMethod() : null,
                finalResolved
        );
    }

    /**
     * Pour chaque paramètre d'interface : valeur = user[codeMethode] ?? user[code] ?? valeur défaut.
     * Publie le rôle méthode et le code API (tous deux issus de la BDD).
     */
    static Map<String, String> bindInterfaceParams(
            List<ParametreUtilisateur> interfaceParams,
            Map<String, String> userParams
    ) {
        Map<String, String> resolved = new LinkedHashMap<>();
        if (interfaceParams == null || interfaceParams.isEmpty()) {
            return resolved;
        }
        for (ParametreUtilisateur p : interfaceParams) {
            String valeur = null;
            if (StringUtils.hasText(p.getCodeParametreMethode())) {
                valeur = userParams.get(p.getCodeParametreMethode());
            }
            if (!StringUtils.hasText(valeur)) {
                valeur = userParams.get(p.getCodeParametre());
            }
            if (!StringUtils.hasText(valeur)) {
                valeur = p.getValeur();
            }
            String outKey = StringUtils.hasText(p.getCodeParametreMethode())
                    ? p.getCodeParametreMethode()
                    : p.getCodeParametre();
            if (StringUtils.hasText(outKey)) {
                // Plusieurs alias partagent un même code méthode (telephone / msisdn /
                // phone / phoneNumber → TELEPHONE). Un alias vide ne doit pas effacer
                // la valeur déjà trouvée par un alias renseigné : sinon le résultat
                // dépend de l'ordre des paramètres en base.
                if (StringUtils.hasText(valeur) || !StringUtils.hasText(resolved.get(outKey))) {
                    resolved.put(outKey, valeur != null ? valeur : "");
                }
            }
            if (StringUtils.hasText(p.getCodeParametre())) {
                resolved.putIfAbsent(p.getCodeParametre(), valeur != null ? valeur : "");
            }
        }
        return resolved;
    }

    /**
     * Après résolution schéma : chaque {@code codeParametre} d'interface reprend la valeur
     * du {@code codeParametreMethode} (ex. CPTDEST LORO → compteCredit), sans liste d'alias Java.
     */
    static void propagateInterfaceAliases(
            List<ParametreUtilisateur> interfaceParams,
            Map<String, String> resolved
    ) {
        if (interfaceParams == null || interfaceParams.isEmpty()) {
            return;
        }
        for (ParametreUtilisateur p : interfaceParams) {
            String code = p.getCodeParametre();
            String method = p.getCodeParametreMethode();
            if (!StringUtils.hasText(code) || !StringUtils.hasText(method)) {
                continue;
            }
            String methodVal = resolved.get(method);
            if (StringUtils.hasText(methodVal)) {
                resolved.put(code, methodVal);
            } else if (StringUtils.hasText(resolved.get(code))) {
                resolved.put(method, resolved.get(code));
            }
        }
    }

    /**
     * Pour chaque slot méthode, préfère les redirections non-identité puis USER, puis {@code compte_fse}.
     */
    void applySchemaBindings(
            Map<String, String> resolved,
            Map<String, String> userParams,
            List<ParametreSchema> bindings,
            LegCompileRequest request,
            String fseTeneur,
            ServiceTypeCode serviceType
    ) {
        applySchemaBindings(resolved, userParams, bindings, request, fseTeneur, serviceType, Map.of());
    }

    void applySchemaBindings(
            Map<String, String> resolved,
            Map<String, String> userParams,
            List<ParametreSchema> bindings,
            LegCompileRequest request,
            String fseTeneur,
            ServiceTypeCode serviceType,
            Map<String, CompteFieldExtractor> extractorsByMethodRole
    ) {
        Map<String, CompteFieldExtractor> extractors =
                extractorsByMethodRole != null ? extractorsByMethodRole : Map.of();

        Map<String, List<ParametreSchema>> byMethod = new LinkedHashMap<>();
        for (ParametreSchema binding : bindings) {
            String methodCode = StringUtils.hasText(binding.getCodeParametreMethode())
                    ? binding.getCodeParametreMethode()
                    : binding.getCodeParametre();
            if (!StringUtils.hasText(methodCode)) {
                continue;
            }
            byMethod.computeIfAbsent(methodCode, k -> new java.util.ArrayList<>()).add(binding);
        }

        for (Map.Entry<String, List<ParametreSchema>> entry : byMethod.entrySet()) {
            String methodCode = entry.getKey();
            CompteFieldExtractor extractor = extractors.getOrDefault(methodCode, extractorRegistry.numeroCompte());
            List<ParametreSchema> candidates = entry.getValue().stream()
                    .sorted(Comparator.comparingInt(b -> isIdentityBinding(b, methodCode) ? 1 : 0))
                    .toList();

            for (ParametreSchema binding : candidates) {
                String value = resolveSchemaValue(
                        binding,
                        userParams,
                        resolved,
                        request,
                        fseTeneur,
                        serviceType,
                        extractor
                );
                if (StringUtils.hasText(value)) {
                    resolved.put(methodCode, value);
                    break;
                }
            }
        }
    }

    private String resolveSchemaValue(
            ParametreSchema binding,
            Map<String, String> userParams,
            Map<String, String> resolved,
            LegCompileRequest request,
            String fseTeneur,
            ServiceTypeCode serviceType,
            CompteFieldExtractor extractor
    ) {
        String etiquette = binding.getCodeParametre();
        String value = firstNonBlank(
                userParams.get(etiquette),
                resolved.get(etiquette)
        );
        if (!StringUtils.hasText(value)) {
            value = lookupCompteField(etiquette, request, fseTeneur, serviceType, extractor);
        }
        if (!StringUtils.hasText(value)) {
            value = binding.getValeurDefaut();
        }
        return value;
    }

    private String lookupCompteField(
            String etiquette,
            LegCompileRequest request,
            String fseTeneur,
            ServiceTypeCode serviceType,
            CompteFieldExtractor extractor
    ) {
        if (!StringUtils.hasText(etiquette)
                || !StringUtils.hasText(request.sourceCodeFse())
                || !StringUtils.hasText(request.destCodeFse())
                || !StringUtils.hasText(fseTeneur)) {
            return null;
        }
        String src = request.sourceCodeFse().trim();
        String dest = request.destCodeFse().trim();
        String teneur = fseTeneur.trim();
        String agence = param(request, "agenceCode", "AGENCE_CODE", "CODE_AGENCE", "agence");
        String codeUo = param(request, "codeUoTitulaire", "CODE_UO_TITULAIRE", "CODE_UO", "codeUo");
        CompteFieldExtractor field = extractor != null ? extractor : extractorRegistry.numeroCompte();

        Optional<CompteFse> compte;
        if (src.equals(dest)) {
            // Same-FSE — Omnicanal findCompteFse(agence, fseTitulaire, etiquette, codeUo, service)
            compte = compteRepository.findCompteFseSameFse(
                    agence,
                    teneur,
                    etiquette,
                    codeUo,
                    serviceType
            );
            if (compte.isEmpty()) {
                compte = compteRepository.findCompteByEtiquetteAndFse(teneur, etiquette);
            }
        } else {
            Optional<RelationFse> relation = relationRepository.findRelation(src, dest, serviceType);
            if (relation.isEmpty()) {
                return null;
            }
            compte = compteRepository.findCompteFse(
                    etiquette,
                    serviceType,
                    relation.get().getId(),
                    teneur,
                    agence
            );
        }
        return compte.map(field::extract).orElse(null);
    }

    private static String param(LegCompileRequest request, String... keys) {
        Map<String, String> params = request.userParams();
        if (params == null || params.isEmpty()) {
            return null;
        }
        for (String key : keys) {
            String v = params.get(key);
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return null;
    }

    Map<String, CompteFieldExtractor> extractorsByMethodRole(List<ParametreUtilisateur> interfaceParams) {
        Map<String, CompteFieldExtractor> out = new LinkedHashMap<>();
        if (interfaceParams == null) {
            return out;
        }
        for (ParametreUtilisateur p : interfaceParams) {
            CompteFieldExtractor extractor = extractorRegistry.resolve(p.getAttribut());
            if (StringUtils.hasText(p.getCodeParametreMethode())) {
                out.putIfAbsent(p.getCodeParametreMethode(), extractor);
            }
            if (StringUtils.hasText(p.getCodeParametre())) {
                out.putIfAbsent(p.getCodeParametre(), extractor);
            }
        }
        return out;
    }

    private static ServiceTypeCode resolveServiceType(LigneSchemaEcriture ligne, String typeMethode) {
        if (ligne.getSchemaEcriture() != null && ligne.getSchemaEcriture().getServiceType() != null) {
            return ligne.getSchemaEcriture().getServiceType();
        }
        return ServiceTypeCode.fromString(typeMethode);
    }

    private static boolean isIdentityBinding(ParametreSchema binding, String methodCode) {
        return Objects.equals(methodCode, binding.getCodeParametre());
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return null;
    }
}
