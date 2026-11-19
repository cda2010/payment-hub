package com.gateway.payment.edge;

/**
 * Classement d'un passage à la porte. Le chemin et le jeton n'entrent pas dans la mesure.
 */
final class EdgeMetrics {

    static final String REQUESTS = "payment.edge.requests";
    static final String LATENCY = "payment.edge.latency";

    private EdgeMetrics() {
    }

    static String outcome(int status, boolean failed, boolean cancelled) {
        if (cancelled) {
            return "cancelled";
        }
        if (failed || status <= 0 || status >= 500) {
            return "error";
        }
        if (status >= 400) {
            return "client_error";
        }
        return "success";
    }
}
