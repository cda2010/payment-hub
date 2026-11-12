package com.gateway.payment.catalog.presentation.datasource.local;

import com.gateway.payment.datasources.CatalogDataSource;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchContext;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import com.gateway.payment.datasources.spi.CatalogDataSourceHandler;
import com.gateway.payment.transfer.service.TransferBeneficiaryService;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TransferBeneficiariesLocalDataSourceHandler implements CatalogDataSourceHandler {

    private final TransferBeneficiaryService transferBeneficiaryService;

    public TransferBeneficiariesLocalDataSourceHandler(TransferBeneficiaryService transferBeneficiaryService) {
        this.transferBeneficiaryService = transferBeneficiaryService;
    }

    @Override
    public Set<String> codes() {
        return Set.of(CatalogDataSource.TRANSFER_BENEFICIARIES.name());
    }

    @Override
    public String label() {
        return "Bénéficiaires enregistrés";
    }

    @Override
    public CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context) {
        int limit = 20;
        Object rawLimit = context.section().params().get("limit");
        if (rawLimit instanceof Number n && n.intValue() > 0) {
            limit = n.intValue();
        }
        return new CatalogDataSourceFetchResult(
                transferBeneficiaryService.listCatalogItems(limit),
                false
        );
    }
}
