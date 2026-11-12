package com.gateway.payment.catalog.omnicanal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Branchement des INVOKE catalogue vers Omnicanal (jambes FSE / SpecDriven).
 * <p>
 * Tout appel fournisseur passe par InterfaceUtilisateur REQUEST + ParametreUtilisateur.
 * Pas de fallback connecteur « direct » (contexte BPM brut).
 */
@ConfigurationProperties(prefix = "gateway.catalog.omnicanal")
public class CatalogOmnicanalProperties {

    /** Active le routage BPMN → InterfaceUtilisateur / Omnicanal. */
    private boolean enabled = true;

    /**
     * Conservé pour compat YAML ; ignoré — le rail direct est supprimé.
     *
     * @deprecated toujours {@code false} effectif
     */
    @Deprecated
    private boolean fallbackToProvider = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** @deprecated rail direct supprimé */
    @Deprecated
    public boolean isFallbackToProvider() {
        return false;
    }

    /** @deprecated rail direct supprimé */
    @Deprecated
    public void setFallbackToProvider(boolean fallbackToProvider) {
        this.fallbackToProvider = false;
    }
}
