package com.gateway.payment.integration.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Ligne d'historique gateway.
 *
 * <p>{@code title} / {@code subtitle} / {@code productLabel} sont formatés serveur
 * selon {@code Accept-Language} — le client affiche tel quel.
 *
 * <p>{@code context} ne contient que des champs métier rejouables / affichables
 * (pas de nextAction, OTP, PIN, ni configuration catalogue interne).
 */
public record GatewayTransactionSummary(
        String paymentId,
        String provider,
        String serviceType,
        String catalogServiceCode,
        BigDecimal amount,
        String currency,
        String status,
        String currentStep,
        String providerReference,
        String customerReference,
        String phoneNumber,
        String errorCode,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        /** Libellé principal relevé — ex. {@code Transfert · vers Aminata · Orange Money}. */
        String title,
        /** Sous-ligne — ex. {@code Validée · motif · 10:42}. */
        String subtitle,
        /** Nom produit localisé (filtre / type). */
        String productLabel,
        /** Statut localisé (ex. {@code Validée} / {@code Completed}). */
        String statusLabel,
        /** Point agent / agence pour le détail (peut être null). */
        String agentDisplay,
        Map<String, Object> context
) {}
