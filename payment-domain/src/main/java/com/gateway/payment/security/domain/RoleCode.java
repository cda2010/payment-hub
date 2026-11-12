package com.gateway.payment.security.domain;

public enum RoleCode {
    ADMIN,
    CONFIG_MANAGER,
    OPERATOR,
    SUPPORT,
    AUDITOR,
    /** Validateur ACCOUNT_TRANSFER — niveau 1 (DMN LEVEL_1). */
    CHEF_AGENCE,
    /** Validateur ACCOUNT_TRANSFER — niveau 2. */
    RESPONSABLE_OPERATIONS,
    /** Validateur ACCOUNT_TRANSFER — niveau 3 (HIGH_VALUE). */
    DIRECTEUR
}
