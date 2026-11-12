package com.gateway.payment.datasources;

import java.util.Map;

/** Item normalisé renvoyé par un gateway peer. */
public record RemoteDataSourceItem(
        String id,
        String label,
        String sub,
        String icon,
        String value,
        Map<String, Object> meta
) {}
