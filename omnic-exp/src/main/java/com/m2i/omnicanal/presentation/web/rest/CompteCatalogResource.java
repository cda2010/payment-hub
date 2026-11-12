package com.m2i.omnicanal.presentation.web.rest;

import com.m2i.omnicanal.presentation.service.FseCatalogQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Comptes lus par payment-2.
 */
@RestController
@RequestMapping("/api/comptes/catalogue")
public class CompteCatalogResource {

    private final FseCatalogQueryService catalog;

    public CompteCatalogResource(FseCatalogQueryService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public List<Map<String, Object>> comptes() {
        return catalog.comptes();
    }
}
