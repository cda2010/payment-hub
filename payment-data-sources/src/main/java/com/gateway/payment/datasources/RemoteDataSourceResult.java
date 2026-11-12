package com.gateway.payment.datasources;

import java.util.List;

public record RemoteDataSourceResult(List<RemoteDataSourceItem> items, boolean hasMore) {

    public static RemoteDataSourceResult empty() {
        return new RemoteDataSourceResult(List.of(), false);
    }

    public RemoteDataSourceResult {
        items = items != null ? List.copyOf(items) : List.of();
    }
}
