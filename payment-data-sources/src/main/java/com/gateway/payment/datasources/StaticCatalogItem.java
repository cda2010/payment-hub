package com.gateway.payment.datasources;

import java.util.Map;

/** Item statique déclaré dans une section présentation / data-source. */
public record StaticCatalogItem(
        String id,
        String label,
        String sub,
        String icon,
        String value,
        Map<String, Object> meta
) {}
