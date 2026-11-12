package com.gateway.payment.bpm;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Mode d'orchestration du checkout.
 * <ul>
 *   <li>{@code engine} (défaut) — {@code PaymentEngine} inchangé</li>
 *   <li>{@code flowable} — réservé ; non branché sur {@code /api/v1/payments}</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "gateway.orchestration")
public class GatewayOrchestrationProperties {

    public enum Mode {
        ENGINE,
        FLOWABLE
    }

    /** Défaut : moteur maison — ne jamais basculer en prod sans bridge explicite. */
    private Mode mode = Mode.ENGINE;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public boolean isEngine() {
        return mode == Mode.ENGINE;
    }

    public boolean isFlowable() {
        return mode == Mode.FLOWABLE;
    }
}
