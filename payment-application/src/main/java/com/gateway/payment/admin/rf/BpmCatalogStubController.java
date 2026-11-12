package com.gateway.payment.admin.rf;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogue BPM stub — actif uniquement si {@code rapidframe.bpm.enabled=false}.
 * Quand Flowable / rapidframe-bpm est activé, le vrai {@code BpmCatalogController} prend le relais.
 */
@RestController
@RequestMapping("/api/bpm")
@ConditionalOnProperty(prefix = "rapidframe.bpm", name = "enabled", havingValue = "false")
public class BpmCatalogStubController {

    private static final String MODULE = "payment-admin-module";

    @GetMapping("/catalog/modules")
    public List<Map<String, Object>> modules() {
        Map<String, Object> mod = new LinkedHashMap<>();
        mod.put("name", MODULE);
        mod.put("slug", "payment-admin");
        mod.put("title", "Administration");
        mod.put("version", "0.1.0");
        mod.put("processCount", 0);
        return List.of(mod);
    }

    @GetMapping("/catalog/processes")
    public List<Map<String, Object>> processes(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String q) {
        return Collections.emptyList();
    }

    @GetMapping("/catalog/processes/{resourceName}")
    public ResponseEntity<Map<String, Object>> open(@PathVariable String resourceName) {
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/catalog/processes/{resourceName}")
    public ResponseEntity<Map<String, Object>> save(
            @PathVariable String resourceName,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", resourceName);
        out.put("moduleName", MODULE);
        out.put("saved", false);
        out.put("error", "BPM désactivé sur payment-gateway — exportez le XML localement.");
        return ResponseEntity.status(501).body(out);
    }

    @GetMapping("/catalog/expression-context")
    public Map<String, Object> expressionContext(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String formKey) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("module", MODULE);
        out.put("formKey", formKey);
        out.put("variables", List.of());
        return out;
    }

    @PostMapping(value = "/expressions/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> validateExpression(@RequestBody(required = false) Map<String, Object> body) {
        return Map.of("valid", true, "message", "ok");
    }
}
