package com.m2i.omnicanal.presentation.web.rest;

import com.m2i.omnicanal.presentation.service.FseCatalogQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogue FSE lu par payment-2 (fournisseur, API, méthodes, interfaces).
 */
@RestController
@RequestMapping("/api/fse")
public class FseCatalogResource {

    private final FseCatalogQueryService catalog;

    public FseCatalogResource(FseCatalogQueryService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/fournisseurs")
    public List<Map<String, Object>> fournisseurs() {
        return catalog.fournisseurs();
    }

    @GetMapping("/{codeFse}/catalog")
    public Map<String, Object> catalog(@PathVariable String codeFse) {
        return catalog.catalog(codeFse);
    }

    @PutMapping("/{codeFse}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String codeFse,
            @RequestBody Map<String, Object> body
    ) {
        try {
            return ResponseEntity.ok(catalog.updateFournisseur(codeFse, body));
        } catch (ResponseStatusException ex) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("message", ex.getReason() == null ? ex.getMessage() : ex.getReason());
            return ResponseEntity.status(ex.getStatusCode()).body(error);
        }
    }
}
