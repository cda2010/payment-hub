package com.gateway.payment.catalog.capability;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Résultat d'une capability — le dispatcher applique vars / events / effet terminal.
 */
public record CapabilityResult(
        boolean handled,
        boolean softRulesMatched,
        List<CapabilityEvent> events,
        Map<String, Object> executionVars,
        TerminalEffect terminal,
        String statusMessage
) {

    public record CapabilityEvent(String type, String message) {}

    public enum TerminalEffect {
        NONE,
        SUCCESS,
        RETURNED_FOR_CORRECTION,
        CANCELLED,
        OTP_REQUIRED,
        REDIRECT_REQUIRED,
        WAITING_CALLBACK,
        PROCESSING
    }

    public static CapabilityResult unhandled() {
        return new CapabilityResult(false, false, List.of(), Map.of(), TerminalEffect.NONE, null);
    }

    public static CapabilityResult of(
            boolean softMatched,
            List<CapabilityEvent> events,
            Map<String, Object> vars,
            TerminalEffect terminal,
            String statusMessage
    ) {
        return new CapabilityResult(
                true,
                softMatched,
                events == null ? List.of() : List.copyOf(events),
                vars == null ? Map.of() : Map.copyOf(vars),
                terminal == null ? TerminalEffect.NONE : terminal,
                statusMessage
        );
    }

    public static CapabilityResult softOnly(boolean softMatched, List<CapabilityEvent> events, Map<String, Object> vars) {
        return of(softMatched, events, vars, TerminalEffect.NONE, null);
    }

    public static CapabilityResult provider(
            List<CapabilityEvent> events,
            Map<String, Object> vars,
            TerminalEffect terminal,
            String statusMessage
    ) {
        return of(false, events, vars, terminal, statusMessage);
    }

    public Map<String, Object> mutableVars() {
        return new LinkedHashMap<>(executionVars);
    }
}
