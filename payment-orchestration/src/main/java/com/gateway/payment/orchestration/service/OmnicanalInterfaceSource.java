package com.gateway.payment.orchestration.service;

import com.gateway.payment.orchestration.api.dto.InterfaceUtilisateurResponse;

import java.util.List;
import java.util.UUID;

/**
 * Lecture des interfaces utilisateur depuis l'omnicanal externe.
 * Absente : payment-2 garde sa copie locale.
 */
public interface OmnicanalInterfaceSource {

    boolean applies(UUID providerId);

    List<InterfaceUtilisateurResponse> list(UUID providerId);
}
