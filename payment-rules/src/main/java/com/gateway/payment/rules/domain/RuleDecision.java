package com.gateway.payment.rules.domain;

/** Politique déterministe : REJECT > REVIEW > ALLOW. */
public enum RuleDecision {
    ALLOW, REVIEW, REJECT;

    public boolean blocks() {
        return this == REJECT;
    }

    public static RuleDecision merge(RuleDecision current, RuleDecision next) {
        if (current == null) {
            return next;
        }
        if (current == REJECT || next == REJECT) {
            return REJECT;
        }
        if (current == REVIEW || next == REVIEW) {
            return REVIEW;
        }
        return ALLOW;
    }
}
