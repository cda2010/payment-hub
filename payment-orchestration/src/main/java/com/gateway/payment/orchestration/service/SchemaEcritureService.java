package com.gateway.payment.orchestration.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.orchestration.api.dto.*;
import com.gateway.payment.orchestration.domain.LigneSchemaEcriture;
import com.gateway.payment.orchestration.domain.ParametreSchema;
import com.gateway.payment.orchestration.domain.SchemaEcriture;
import com.gateway.payment.orchestration.repository.LigneSchemaEcritureRepository;
import com.gateway.payment.orchestration.repository.ParametreSchemaRepository;
import com.gateway.payment.orchestration.repository.SchemaEcritureRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SchemaEcritureService {

    private final SchemaEcritureRepository schemaRepository;
    private final LigneSchemaEcritureRepository ligneRepository;
    private final ParametreSchemaRepository parametreRepository;
    private final OrchestrationMapper mapper;
    private final AuditService auditService;
    private final ObjectProvider<OmnicanalSchemaSource> schemaSource;

    public SchemaEcritureService(
            SchemaEcritureRepository schemaRepository,
            LigneSchemaEcritureRepository ligneRepository,
            ParametreSchemaRepository parametreRepository,
            OrchestrationMapper mapper,
            AuditService auditService,
            ObjectProvider<OmnicanalSchemaSource> schemaSource
    ) {
        this.schemaRepository = schemaRepository;
        this.ligneRepository = ligneRepository;
        this.parametreRepository = parametreRepository;
        this.mapper = mapper;
        this.auditService = auditService;
        this.schemaSource = schemaSource;
    }

    @Transactional(readOnly = true)
    public List<SchemaEcritureResponse> listAll() {
        OmnicanalSchemaSource source = remote();
        if (source != null) {
            return source.list(null);
        }
        return schemaRepository.findAllByOrderByServiceTypeAscNomSchemaAsc().stream()
                .map(s -> mapper.toResponse(s, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SchemaEcritureResponse> listByServiceType(ServiceTypeCode serviceType) {
        OmnicanalSchemaSource source = remote();
        if (source != null) {
            return source.list(serviceType);
        }
        return schemaRepository.findByServiceTypeAndActifTrueOrderByNomSchemaAsc(serviceType).stream()
                .map(s -> mapper.toResponse(s, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public SchemaEcritureResponse getById(UUID id) {
        OmnicanalSchemaSource source = remote();
        if (source != null) {
            Optional<SchemaEcritureResponse> remoteSchema = source.get(id);
            if (remoteSchema.isPresent()) {
                return remoteSchema.get();
            }
        }
        SchemaEcriture schema = schemaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SCHEMA_NOT_FOUND", "Schéma introuvable", HttpStatus.NOT_FOUND));
        List<LigneSchemaEcriture> lignes = ligneRepository.findBySchemaIdWithParametres(id);
        schema.setLignes(lignes);
        return mapper.toResponse(schema, true);
    }

    @Transactional
    public SchemaEcritureResponse create(SchemaEcritureRequest request, String actor) {
        schemaRepository.findByServiceTypeAndPatternCode(request.serviceType(), request.patternCode())
                .ifPresent(existing -> {
                    throw new BusinessException(
                            "SCHEMA_EXISTS",
                            "Schéma déjà défini pour " + request.serviceType() + " / " + request.patternCode()
                    );
                });
        // Nouveau schéma : pas encore de lignes → forcer inactif ; publication via /publish.
        boolean actif = false;
        SchemaEcriture entity = SchemaEcriture.builder()
                .nomSchema(request.nomSchema())
                .serviceType(request.serviceType())
                .patternCode(request.patternCode().trim().toUpperCase())
                .workflowName(request.workflowName())
                .workflowVersion(request.workflowVersion() != null ? request.workflowVersion() : 1)
                .engine(StringUtils.hasText(request.engine()) ? request.engine() : "flowable")
                .errorPolicyCode(request.errorPolicyCode())
                .actif(actif)
                .build();
        SchemaEcriture saved = schemaRepository.save(entity);
        auditService.record(actor, "SCHEMA_CREATE", "SCHEMA_ECRITURE", saved.getId().toString(),
                saved.getNomSchema() + " / " + saved.getPatternCode());
        return mapper.toResponse(saved, false);
    }

    @Transactional
    public SchemaEcritureResponse update(UUID id, SchemaEcritureRequest request, String actor) {
        SchemaEcriture entity = requireSchema(id);
        boolean wasActif = entity.isActif();
        entity.setNomSchema(request.nomSchema());
        entity.setServiceType(request.serviceType());
        entity.setPatternCode(request.patternCode().trim().toUpperCase());
        entity.setWorkflowName(request.workflowName());
        entity.setWorkflowVersion(request.workflowVersion() != null ? request.workflowVersion() : 1);
        entity.setEngine(StringUtils.hasText(request.engine()) ? request.engine() : "flowable");
        entity.setErrorPolicyCode(request.errorPolicyCode());
        if (request.actif()) {
            assertPublishable(id);
            entity.setActif(true);
        } else {
            entity.setActif(false);
        }
        SchemaEcriture saved = schemaRepository.save(entity);
        String action = !wasActif && saved.isActif() ? "SCHEMA_PUBLISH"
                : wasActif && !saved.isActif() ? "SCHEMA_UNPUBLISH"
                : "SCHEMA_UPDATE";
        auditService.record(actor, action, "SCHEMA_ECRITURE", saved.getId().toString(),
                saved.getNomSchema() + " / actif=" + saved.isActif());
        return mapper.toResponse(saved, false);
    }

    /** Active le schéma après contrôle de complétude. */
    @Transactional
    public SchemaEcritureResponse publish(UUID id, String actor) {
        assertPublishable(id);
        SchemaEcriture entity = requireSchema(id);
        entity.setActif(true);
        SchemaEcriture saved = schemaRepository.save(entity);
        auditService.record(actor, "SCHEMA_PUBLISH", "SCHEMA_ECRITURE", id.toString(),
                saved.getNomSchema() + " / " + saved.getPatternCode());
        return mapper.toResponse(saved, false);
    }

    /** Désactive le schéma (runtime ignore les schémas inactifs). */
    @Transactional
    public SchemaEcritureResponse unpublish(UUID id, String actor) {
        SchemaEcriture entity = requireSchema(id);
        entity.setActif(false);
        SchemaEcriture saved = schemaRepository.save(entity);
        auditService.record(actor, "SCHEMA_UNPUBLISH", "SCHEMA_ECRITURE", id.toString(),
                saved.getNomSchema());
        return mapper.toResponse(saved, false);
    }

    @Transactional
    public void delete(UUID id, String actor) {
        SchemaEcriture entity = requireSchema(id);
        auditService.record(actor, "SCHEMA_DELETE", "SCHEMA_ECRITURE", id.toString(), entity.getNomSchema());
        schemaRepository.delete(entity);
    }

    /**
     * Règles de publication : ≥1 ligne, chaque ligne a typeMethode + apiAUtiliser,
     * workflowName renseigné.
     */
    @Transactional(readOnly = true)
    public void assertPublishable(UUID schemaId) {
        SchemaEcriture schema = requireSchema(schemaId);
        List<String> errors = new ArrayList<>();
        if (!StringUtils.hasText(schema.getWorkflowName())) {
            errors.add("workflowName manquant");
        }
        List<LigneSchemaEcriture> lignes = ligneRepository.findBySchemaIdWithParametres(schemaId);
        if (lignes.isEmpty()) {
            errors.add("au moins une ligne est requise");
        }
        for (LigneSchemaEcriture ligne : lignes) {
            String label = "ligne #" + ligne.getOrdre();
            if (!StringUtils.hasText(ligne.getTypeMethode())) {
                errors.add(label + " : typeMethode manquant");
            }
            if (ligne.getApiAUtiliser() == null) {
                errors.add(label + " : apiAUtiliser manquant");
            }
        }
        if (!errors.isEmpty()) {
            throw new BusinessException(
                    "SCHEMA_NOT_PUBLISHABLE",
                    "Schéma incomplet : " + String.join(" ; ", errors),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @Transactional
    public LigneSchemaEcritureResponse addLigne(LigneSchemaEcritureRequest request) {
        SchemaEcriture schema = requireSchema(request.schemaEcritureId());
        LigneSchemaEcriture ligne = LigneSchemaEcriture.builder().schemaEcriture(schema).build();
        applyLigne(ligne, request);
        return mapper.toLigneResponse(ligneRepository.save(ligne));
    }

    @Transactional
    public LigneSchemaEcritureResponse updateLigne(UUID id, LigneSchemaEcritureRequest request) {
        LigneSchemaEcriture ligne = requireLigne(id);
        applyLigne(ligne, request);
        return mapper.toLigneResponse(ligneRepository.save(ligne));
    }

    @Transactional
    public void deleteLigne(UUID id) {
        ligneRepository.delete(requireLigne(id));
    }

    @Transactional(readOnly = true)
    public List<LigneSchemaEcritureResponse> listLignes(UUID schemaId) {
        OmnicanalSchemaSource source = remote();
        if (source != null) {
            Optional<List<LigneSchemaEcritureResponse>> remoteLines = source.lignes(schemaId);
            if (remoteLines.isPresent()) {
                return remoteLines.get();
            }
        }
        requireSchema(schemaId);
        return ligneRepository.findBySchemaIdWithParametres(schemaId).stream()
                .map(mapper::toLigneResponse)
                .toList();
    }

    @Transactional
    public ParametreSchemaResponse addParametre(ParametreSchemaRequest request) {
        LigneSchemaEcriture ligne = requireLigne(request.ligneSchemaEcritureId());
        ParametreSchema param = ParametreSchema.builder()
                .ligneSchemaEcriture(ligne)
                .nomParametre(request.nomParametre())
                .codeParametre(request.codeParametre())
                .codeParametreMethode(StringUtils.hasText(request.codeParametreMethode())
                        ? request.codeParametreMethode()
                        : request.codeParametre())
                .nomExterne(request.nomExterne())
                .typeParametre(request.typeParametre())
                .categorie(request.categorie())
                .valeurDefaut(request.valeurDefaut())
                .build();
        return mapper.toParametreResponse(parametreRepository.save(param));
    }

    @Transactional
    public void deleteParametre(UUID id) {
        parametreRepository.deleteById(id);
    }

    @Transactional
    public ParametreSchemaResponse updateParametre(UUID id, ParametreSchemaRequest request) {
        ParametreSchema param = parametreRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PARAM_NOT_FOUND", "Paramètre introuvable", HttpStatus.NOT_FOUND));
        param.setNomParametre(request.nomParametre());
        param.setCodeParametre(request.codeParametre());
        param.setCodeParametreMethode(StringUtils.hasText(request.codeParametreMethode())
                ? request.codeParametreMethode()
                : request.codeParametre());
        param.setNomExterne(request.nomExterne());
        param.setTypeParametre(request.typeParametre());
        param.setCategorie(request.categorie());
        param.setValeurDefaut(request.valeurDefaut());
        return mapper.toParametreResponse(parametreRepository.save(param));
    }

    @Transactional(readOnly = true)
    public List<ParametreSchemaResponse> listParametres(UUID ligneId) {
        OmnicanalSchemaSource source = remote();
        if (source != null) {
            Optional<List<ParametreSchemaResponse>> remoteParams = source.parametres(ligneId);
            if (remoteParams.isPresent()) {
                return remoteParams.get();
            }
        }
        requireLigne(ligneId);
        return parametreRepository.findByLigneSchemaEcritureIdOrderByCodeParametreAsc(ligneId).stream()
                .map(mapper::toParametreResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LigneSchemaEcritureResponse getLigne(UUID id) {
        OmnicanalSchemaSource source = remote();
        if (source != null) {
            Optional<LigneSchemaEcritureResponse> remoteLine = source.ligne(id);
            if (remoteLine.isPresent()) {
                return remoteLine.get();
            }
        }
        return mapper.toLigneResponse(ligneRepository.findByIdWithParametres(id)
                .orElseThrow(() -> new BusinessException("LIGNE_NOT_FOUND", "Ligne de schéma introuvable", HttpStatus.NOT_FOUND)));
    }

    private OmnicanalSchemaSource remote() {
        OmnicanalSchemaSource source = schemaSource.getIfAvailable();
        if (source == null || !source.enabled()) {
            return null;
        }
        return source;
    }

    private void applyLigne(LigneSchemaEcriture ligne, LigneSchemaEcritureRequest request) {
        ligne.setOrdre(request.ordre());
        ligne.setStepId(blankToNull(request.stepId()));
        ligne.setTypeMethode(request.typeMethode().trim().toUpperCase());
        ligne.setApiAUtiliser(request.apiAUtiliser());
        ligne.setAgenceImputer(request.agenceImputer());
        ligne.setExecutionMode(blankToNull(request.executionMode()));
        ligne.setTimeoutPolicy(blankToNull(request.timeoutPolicy()));
        ligne.setTimeoutDuration(blankToNull(request.timeoutDuration()));
        ligne.setCallbackPolicy(blankToNull(request.callbackPolicy()));
        ligne.setStatusMethod(blankToNull(request.statusMethod()));
        ligne.setRetryPolicy(blankToNull(request.retryPolicy()));
        ligne.setRetryMaxAttempts(request.retryMaxAttempts());
        ligne.setRetryInitialDelayMs(request.retryInitialDelayMs());
        ligne.setRetryMultiplier(request.retryMultiplier());
        ligne.setRetryMaxDelayMs(request.retryMaxDelayMs());
        ligne.setRetryableCodes(blankToNull(request.retryableCodes()));
        ligne.setIdempotencyPolicy(blankToNull(request.idempotencyPolicy()));
        ligne.setCompensationPolicy(blankToNull(request.compensationPolicy()));
        ligne.setCompensationMode(blankToNull(request.compensationMode()));
        ligne.setCompensationStepCode(blankToNull(request.compensationStepCode()));
        ligne.setCompensationReverseDesc(request.compensationReverseDesc());
        ligne.setCompensationMaxAttempts(request.compensationMaxAttempts());
        ligne.setCompensationManualReview(request.compensationManualReview());
        ligne.setDependsOnStepCode(blankToNull(request.dependsOnStepCode()));
        ligne.setDependencyCondition(blankToNull(request.dependencyCondition()));
        ligne.setDependencyRequired(request.dependencyRequired());
        ligne.setErrorPolicies(blankToNull(request.errorPolicies()));
        ligne.setErrorPolicyCode(blankToNull(request.errorPolicyCode()));
        ligne.setDefaultErrorAction(blankToNull(request.defaultErrorAction()));
        ligne.setCheckStatusMethodCode(blankToNull(request.checkStatusMethodCode()));
        ligne.setMaxRetries(request.maxRetries());
        ligne.setBlocking(request.blocking());
        ligne.setCriticality(blankToNull(request.criticality()));
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    SchemaEcriture requireSchema(UUID id) {
        return schemaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SCHEMA_NOT_FOUND", "Schéma introuvable", HttpStatus.NOT_FOUND));
    }

    LigneSchemaEcriture requireLigne(UUID id) {
        return ligneRepository.findById(id)
                .orElseThrow(() -> new BusinessException("LIGNE_NOT_FOUND", "Ligne de schéma introuvable", HttpStatus.NOT_FOUND));
    }
}
