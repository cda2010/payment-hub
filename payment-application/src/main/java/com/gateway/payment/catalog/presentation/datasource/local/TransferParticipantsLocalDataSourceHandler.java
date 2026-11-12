package com.gateway.payment.catalog.presentation.datasource.local;

import com.gateway.payment.datasources.CatalogDataSource;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchContext;
import com.gateway.payment.datasources.spi.CatalogDataSourceFetchResult;
import com.gateway.payment.datasources.spi.CatalogDataSourceHandler;
import com.gateway.payment.catalog.presentation.datasource.CatalogLocalItemMapper;
import com.gateway.payment.transfer.service.TransferCatalogService;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TransferParticipantsLocalDataSourceHandler implements CatalogDataSourceHandler {

    private final TransferCatalogService transferCatalogService;

    public TransferParticipantsLocalDataSourceHandler(TransferCatalogService transferCatalogService) {
        this.transferCatalogService = transferCatalogService;
    }

    @Override
    public Set<String> codes() {
        return Set.of(CatalogDataSource.TRANSFER_PARTICIPANTS.name());
    }

    @Override
    public String label() {
        return "Participants transfert";
    }

    @Override
    public CatalogDataSourceFetchResult fetch(CatalogDataSourceFetchContext context) {
        return new CatalogDataSourceFetchResult(
                CatalogLocalItemMapper.mapParticipants(
                        transferCatalogService.listParticipants(true),
                        context.section().params()
                ),
                false
        );
    }
}
