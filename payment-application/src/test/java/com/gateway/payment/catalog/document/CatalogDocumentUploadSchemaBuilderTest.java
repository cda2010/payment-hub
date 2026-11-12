package com.gateway.payment.catalog.document;

import com.gateway.payment.bpm.dmn.DmnDocumentRequirementsConfig;
import com.gateway.payment.catalog.casting.CatalogFormSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogDocumentUploadSchemaBuilderTest {

    @Test
    void build_createsFileFieldsForRequiredDocs() {
        CatalogDocumentUploadSchemaBuilder builder = new CatalogDocumentUploadSchemaBuilder();
        Map<String, Object> context = Map.of(
                DmnDocumentRequirementsConfig.VAR_PROFILE_CODE, "STANDARD",
                DmnDocumentRequirementsConfig.VAR_REQUIRED_DOCS, List.of(
                        Map.of("code", "ID_FRONT", "label", "CNI", "mimeTypes", List.of("image/jpeg"), "maxSizeMb", 5),
                        Map.of("code", "PROOF_OF_ADDRESS", "label", "Domicile", "mimeTypes", List.of("application/pdf"), "maxSizeMb", 5)
                )
        );
        Map<String, Object> schema = builder.build(context);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> components = (List<Map<String, Object>>) schema.get("components");
        long files = components.stream().filter(c -> "file".equals(c.get("type"))).count();
        assertEquals(2, files);
        assertTrue(components.stream().anyMatch(c -> "documents.ID_FRONT".equals(c.get("key"))));
    }

    @Test
    void shouldRender_whenFormSourceDocuments() {
        CatalogDocumentUploadSchemaBuilder builder = new CatalogDocumentUploadSchemaBuilder();
        assertTrue(builder.shouldRenderDocuments(
                CatalogFormSource.DOCUMENTS, null, "UPLOAD_DOCUMENTS", Map.of()));
    }
}
