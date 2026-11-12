package com.gateway.payment.omnicanal.external;

import com.gateway.payment.orchestration.api.dto.InterfaceUtilisateurResponse;
import com.gateway.payment.orchestration.service.OmnicanalInterfaceSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ExternalOmnicanalInterfaceSource implements OmnicanalInterfaceSource {

    private final OmnicanalProviderCatalog catalog;

    public ExternalOmnicanalInterfaceSource(OmnicanalProviderCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public boolean applies(UUID providerId) {
        return catalog.supplies(providerId);
    }

    @Override
    public List<InterfaceUtilisateurResponse> list(UUID providerId) {
        List<InterfaceUtilisateurResponse> rows = catalog.interfaces(providerId);
        return rows == null ? List.of() : rows;
    }
}
