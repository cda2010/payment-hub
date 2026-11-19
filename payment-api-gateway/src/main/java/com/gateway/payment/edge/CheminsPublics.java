package com.gateway.payment.edge;

/**
 * Chemins que les services laissent passer sans jeton.
 * Un jeton présent sur ces chemins est quand même vérifié.
 */
final class CheminsPublics {

    private CheminsPublics() {
    }

    static boolean ouvert(String method, String path) {
        if ("OPTIONS".equals(method)) {
            return true;
        }
        if (path.startsWith("/api/v1/identity/enrolement")) {
            return true;
        }
        if (path.startsWith("/api/v1/auth")) {
            return true;
        }
        if ("/api/v1/ping".equals(path) || path.startsWith("/api/v1/i18n")) {
            return true;
        }
        if ("POST".equals(method) && path.startsWith("/api/v1/webhooks")) {
            return true;
        }
        if ("POST".equals(method) && "/api/v1/external-omnicanal/boundary".equals(path)) {
            return true;
        }
        if ("GET".equals(method) && "/api/v1/network/fse".equals(path)) {
            return true;
        }
        if (path.startsWith("/api/v1/content/media")) {
            return true;
        }
        return "/api/v1/boundaryByTypeMethode".equals(path)
                || "/api/boundaryByTypeMethode".equals(path)
                || "/api/v1/boundary".equals(path)
                || "/api/boundary".equals(path)
                || "/api/v1/omnicanalPresentation".equals(path)
                || "/api/omnicanalPresentation".equals(path)
                || path.startsWith("/api/v1/canal");
    }
}
