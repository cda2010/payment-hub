package com.gateway.payment.orchestration.service;

import com.gateway.payment.orchestration.api.dto.CompteFseResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Lecture des comptes catalogue depuis l'omnicanal externe.
 * Absente : payment-2 garde sa copie locale.
 */
public interface OmnicanalCompteSource {

    boolean enabled();

    List<CompteFseResponse> list();

    Optional<CompteFseResponse> get(UUID id);
}
