package com.gateway.payment.orchestration.service;

import com.gateway.payment.orchestration.api.dto.*;
import com.gateway.payment.orchestration.domain.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OrchestrationMapper {

    public SchemaEcritureResponse toResponse(SchemaEcriture entity, boolean includeDetails) {
        List<LigneSchemaEcritureResponse> lignes = includeDetails && entity.getLignes() != null
                ? entity.getLignes().stream().map(this::toLigneResponse).toList()
                : Collections.emptyList();
        return new SchemaEcritureResponse(
                entity.getId(),
                entity.getNomSchema(),
                entity.getServiceType(),
                entity.getPatternCode(),
                entity.getWorkflowName(),
                entity.getWorkflowVersion(),
                entity.getEngine(),
                entity.getErrorPolicyCode(),
                entity.isActif(),
                entity.getCreatedAt(),
                lignes
        );
    }

    public LigneSchemaEcritureResponse toLigneResponse(LigneSchemaEcriture entity) {
        List<ParametreSchemaResponse> params = entity.getParametres() != null
                ? entity.getParametres().stream().map(this::toParametreResponse).toList()
                : Collections.emptyList();
        return new LigneSchemaEcritureResponse(
                entity.getId(),
                entity.getSchemaEcriture() != null ? entity.getSchemaEcriture().getId() : null,
                entity.getOrdre(),
                entity.getStepId(),
                entity.getTypeMethode(),
                entity.getApiAUtiliser(),
                entity.getAgenceImputer(),
                entity.getExecutionMode(),
                entity.getTimeoutPolicy(),
                entity.getTimeoutDuration(),
                entity.getCallbackPolicy(),
                entity.getStatusMethod(),
                entity.getRetryPolicy(),
                entity.getRetryMaxAttempts(),
                entity.getRetryInitialDelayMs(),
                entity.getRetryMultiplier(),
                entity.getRetryMaxDelayMs(),
                entity.getRetryableCodes(),
                entity.getIdempotencyPolicy(),
                entity.getCompensationPolicy(),
                entity.getCompensationMode(),
                entity.getCompensationStepCode(),
                entity.getCompensationReverseDesc(),
                entity.getCompensationMaxAttempts(),
                entity.getCompensationManualReview(),
                entity.getDependsOnStepCode(),
                entity.getDependencyCondition(),
                entity.getDependencyRequired(),
                entity.getErrorPolicies(),
                entity.getErrorPolicyCode(),
                entity.getDefaultErrorAction(),
                entity.getCheckStatusMethodCode(),
                entity.getMaxRetries(),
                entity.getBlocking(),
                entity.getCriticality(),
                entity.getCreatedAt(),
                params
        );
    }

    public ParametreSchemaResponse toParametreResponse(ParametreSchema entity) {
        return new ParametreSchemaResponse(
                entity.getId(),
                entity.getLigneSchemaEcriture() != null ? entity.getLigneSchemaEcriture().getId() : null,
                entity.getNomParametre(),
                entity.getCodeParametre(),
                entity.getCodeParametreMethode(),
                entity.getNomExterne(),
                entity.getTypeParametre(),
                entity.getCategorie(),
                entity.getValeurDefaut(),
                entity.getCreatedAt()
        );
    }

    public InterfaceUtilisateurResponse toResponse(InterfaceUtilisateur entity) {
        List<ParametreUtilisateurResponse> params = entity.getParametres() != null
                ? entity.getParametres().stream().map(this::toParametreUtilisateurResponse).toList()
                : Collections.emptyList();
        return new InterfaceUtilisateurResponse(
                entity.getId(),
                entity.getProvider().getId(),
                entity.getProvider().getCode(),
                entity.getCodeFse(),
                entity.getServiceType(),
                entity.getTypeMethode(),
                entity.getTypeEnvoi(),
                entity.getProviderOperation() != null ? entity.getProviderOperation().getId() : null,
                entity.getProviderOperation() != null ? entity.getProviderOperation().getCode() : null,
                entity.getProviderApi() != null ? entity.getProviderApi().getId() : null,
                entity.getProviderApi() != null ? entity.getProviderApi().getCode() : null,
                entity.getCodeInterface(),
                entity.getNomInterface(),
                entity.isActif(),
                entity.getCreatedAt(),
                params
        );
    }

    public ParametreUtilisateurResponse toParametreUtilisateurResponse(ParametreUtilisateur entity) {
        return new ParametreUtilisateurResponse(
                entity.getId(),
                entity.getInterfaceUtilisateur() != null ? entity.getInterfaceUtilisateur().getId() : null,
                entity.getNomParametre(),
                entity.getCodeParametre(),
                entity.getCodeParametreMethode(),
                entity.getTypeParametre(),
                entity.getValeur(),
                entity.getParametreApi(),
                entity.getAttribut(),
                entity.getSourceParametre(),
                entity.getExtractScript(),
                entity.getLabelKey(),
                entity.getLabelsJson(),
                entity.isAffiche(),
                entity.getOrdre(),
                entity.getCreatedAt()
        );
    }
}
