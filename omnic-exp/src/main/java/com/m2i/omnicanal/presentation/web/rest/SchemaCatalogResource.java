package com.m2i.omnicanal.presentation.web.rest;

import com.m2i.omnicanal.presentation.service.FseCatalogQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Schémas d'écriture lus par payment-2.
 */
@RestController
@RequestMapping("/api/schemas")
public class SchemaCatalogResource {

    private final FseCatalogQueryService catalog;

    public SchemaCatalogResource(FseCatalogQueryService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public List<Map<String, Object>> schemas() {
        return catalog.schemas();
    }
}
