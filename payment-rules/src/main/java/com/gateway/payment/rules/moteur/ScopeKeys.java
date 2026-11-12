package com.gateway.payment.rules.moteur;

import com.gateway.payment.rules.domain.LimitScope;

public final class ScopeKeys {
    private ScopeKeys() {}

    public static String key(LimitScope scope, DemandeEvaluation d) {
        LimitScope s = scope == null ? LimitScope.CUSTOMER : scope;
        return switch (s) {
            case CUSTOMER -> "CUSTOMER:" + nullToUnknown(d.customerId());
            case WALLET -> "WALLET:" + nullToUnknown(first(d.walletId(), d.ctx("walletId")));
            case ACCOUNT -> "ACCOUNT:" + nullToUnknown(d.ctx("accountId"));
            case AGENT -> "AGENT:" + nullToUnknown(first(d.agentId(), d.ctx("agentId")));
            case MERCHANT -> "MERCHANT:" + nullToUnknown(first(d.merchantId(), d.ctx("merchantId")));
            case TILL -> "TILL:" + nullToUnknown(d.ctx("tillId"));
            case UNITE -> "UNITE:" + nullToUnknown(d.ctx("uniteId"));
        };
    }

    private static String first(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }

    private static String nullToUnknown(String v) {
        return v == null || v.isBlank() ? "UNKNOWN" : v;
    }
}
