package com.gateway.payment.orchestration.service;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.orchestration.api.dto.LigneSchemaEcritureResponse;
import com.gateway.payment.orchestration.api.dto.ParametreSchemaResponse;
import com.gateway.payment.orchestration.api.dto.SchemaEcritureResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Lecture des schémas d'écriture depuis l'omnicanal externe.
 * Absente : payment-2 garde sa copie locale.
 */
public interface OmnicanalSchemaSource {

    boolean enabled();

    List<SchemaEcritureResponse> list(ServiceTypeCode serviceType);

    Optional<SchemaEcritureResponse> get(UUID id);

    Optional<List<LigneSchemaEcritureResponse>> lignes(UUID schemaId);

    Optional<LigneSchemaEcritureResponse> ligne(UUID id);

    Optional<List<ParametreSchemaResponse>> parametres(UUID ligneId);
}
