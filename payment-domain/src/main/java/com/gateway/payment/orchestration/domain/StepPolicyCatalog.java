package com.gateway.payment.orchestration.domain;

import java.util.List;

/** Codes de politiques portés par {@link LigneSchemaEcriture} (aligné Omnicanal StepPolicyCodes). */
public final class StepPolicyCatalog {

    private StepPolicyCatalog() {
    }

    public static final List<String> TYPE_METHODES = List.of(
            "DEBIT", "CREDIT", "TRANSFERT", "CONSULTATION", "CONSULTATIONSOLDE", "NOP"
    );

    public static final List<String> EXECUTION_MODES = List.of(
            "SYNC", "ASYNC_CALLBACK", "ASYNC_POLL", "FIRE_AND_FORGET"
    );

    public static final List<String> TIMEOUT_POLICIES = List.of(
            "SHORT_3S", "STANDARD_5S", "FINANCIAL_15S", "LONG_30S"
    );

    public static final List<String> CALLBACK_POLICIES = List.of("NONE", "HTTP_CALLBACK", "POLL_STATUS");

    public static final List<String> RETRY_POLICIES = List.of("NONE", "TRANSIENT_3X", "TRANSIENT_2X_BACKOFF");

    public static final List<String> IDEMPOTENCY_POLICIES = List.of("NONE", "KEY_REFERENCE", "KEY_OP_STEP");

    public static final List<String> COMPENSATION_POLICIES = List.of("NONE", "AUTO_REVERSE", "MANUAL");

    public static final List<String> COMPENSATION_MODES = List.of("NONE", "AUTO_REVERSE", "MANUAL", "REVERSE_DESC");

    public static final List<String> CRITICALITIES = List.of("CRITICAL", "REQUIRED", "OPTIONAL", "INFORMATIONAL");

    public static final List<String> ERROR_ACTIONS = List.of(
            "STOP_WORKFLOW", "RETRY", "CHECK_STATUS", "WAIT_CALLBACK",
            "COMPENSATE", "MANUAL_REVIEW", "ESCALATE", "CONTINUE", "PROPAGATE"
    );

    public static final List<String> DEPENDENCY_CONDITIONS = List.of("ON_SUCCESS", "ON_FAILURE", "ALWAYS");

    public static final List<String> PARAM_TYPES = List.of("STRING", "DOUBLE", "INTEGER", "FLOAT", "LONG");
}
