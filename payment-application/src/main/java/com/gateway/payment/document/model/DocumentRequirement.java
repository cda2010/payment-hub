package com.gateway.payment.document.model;

import java.util.List;

/**
 * Exigence d'une pièce (profil document).
 */
public record DocumentRequirement(
        String code,
        String label,
        List<String> mimeTypes,
        int maxSizeMb
) {}
