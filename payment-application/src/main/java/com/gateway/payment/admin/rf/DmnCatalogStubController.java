package com.gateway.payment.admin.rf;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Catalogue DMN classpath — actif uniquement si {@code rapidframe.bpm.enabled=false}.
 * Quand BPM est actif, le catalogue RapidFrame ({@code DmnCatalogController}) prend le relais.
 */
@RestController
@RequestMapping("/api/dmn")
@ConditionalOnProperty(prefix = "rapidframe.bpm", name = "enabled", havingValue = "false")
public class DmnCatalogStubController {

    private static final String MODULE = "payment-admin-module";
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @GetMapping("/catalog/modules")
    public List<Map<String, Object>> modules() throws IOException {
        Map<String, Object> mod = new LinkedHashMap<>();
        mod.put("name", MODULE);
        mod.put("slug", "payment-admin");
        mod.put("title", "Administration");
        mod.put("version", "0.1.0");
        mod.put("decisionCount", listDmnResources().size());
        return List.of(mod);
    }

    @GetMapping("/catalog/decisions")
    public List<Map<String, Object>> decisions(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String q) throws IOException {
        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Resource resource : listDmnResources()) {
            String filename = resource.getFilename();
            if (filename == null) {
                continue;
            }
            String key = stripExt(filename);
            if (!needle.isEmpty()
                    && !key.toLowerCase(Locale.ROOT).contains(needle)
                    && !filename.toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", filename);
            row.put("title", humanTitle(key));
            row.put("decisionKey", key);
            row.put("moduleName", MODULE);
            row.put("sourceFile", filename);
            row.put("source", "file");
            out.add(row);
        }
        return out;
    }

    @GetMapping("/catalog/decisions/{resourceName}")
    public ResponseEntity<Map<String, Object>> open(
            @PathVariable String resourceName,
            @RequestParam(required = false) String module) throws IOException {
        Resource match = findDmn(resourceName);
        if (match == null || !match.exists()) {
            return ResponseEntity.notFound().build();
        }
        String filename = match.getFilename() != null ? match.getFilename() : resourceName;
        String key = stripExt(filename);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", filename);
        body.put("title", humanTitle(key));
        body.put("decisionKey", key);
        body.put("moduleName", MODULE);
        body.put("sourceFile", filename);
        body.put("source", "file");
        body.put("xml", readUtf8(match));
        return ResponseEntity.ok(body);
    }

    @PutMapping("/catalog/decisions/{resourceName}")
    public ResponseEntity<Map<String, Object>> save(
            @PathVariable String resourceName,
            @RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "status", "read-only",
                "error", "DMN payment-gateway en classpath (lecture seule). Exportez le XML localement."
        ));
    }

    private List<Resource> listDmnResources() throws IOException {
        List<Resource> all = new ArrayList<>();
        all.addAll(Arrays.asList(resolver.getResources("classpath*:rules/*.dmn")));
        all.addAll(Arrays.asList(resolver.getResources("classpath*:modules/*/decisions/**/*.dmn")));
        // dédupliquer par nom de fichier
        Map<String, Resource> byName = new LinkedHashMap<>();
        for (Resource r : all) {
            if (r.getFilename() != null) {
                byName.putIfAbsent(r.getFilename(), r);
            }
        }
        return List.copyOf(byName.values());
    }

    private Resource findDmn(String resourceName) throws IOException {
        String want = resourceName == null ? "" : resourceName.trim();
        if (!want.contains(".")) {
            want = want + ".dmn";
        }
        for (Resource r : listDmnResources()) {
            String fn = r.getFilename();
            if (fn == null) {
                continue;
            }
            if (want.equalsIgnoreCase(fn) || stripExt(want).equalsIgnoreCase(stripExt(fn))) {
                return r;
            }
        }
        return null;
    }

    private static String stripExt(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private static String humanTitle(String key) {
        return switch (key) {
            case "schema-matching" -> "Matching schéma (DMN)";
            case "workflow-select" -> "Sélection workflow (DMN)";
            default -> key.replace('_', ' ').replace('-', ' ');
        };
    }

    private static String readUtf8(Resource resource) throws IOException {
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
