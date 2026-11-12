package com.gateway.payment.documents.api;

import com.gateway.payment.documents.api.dto.DocumentDtos.DocumentView;
import com.gateway.payment.documents.api.dto.DocumentDtos.StatusUpdate;
import com.gateway.payment.documents.api.dto.DocumentDtos.UploadMeta;
import com.gateway.payment.documents.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documents;

    public DocumentController(DocumentService documents) {
        this.documents = documents;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Uploader un document (métadonnées + fichier)")
    public DocumentView upload(
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("meta") UploadMeta meta,
            Authentication authentication
    ) {
        String createdBy = authentication != null ? authentication.getName() : null;
        return documents.upload(file, meta, createdBy);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Métadonnées d'un document")
    public DocumentView get(@PathVariable UUID id) {
        return documents.get(id);
    }

    @GetMapping("/{id}/file")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Télécharger le fichier d'un document")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        Resource resource = documents.loadFile(id);
        MediaType type = documents.mediaType(id);
        String name = documents.originalName(id).replace("\"", "");
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                .body(resource);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les documents (paymentId ou ownerKind+ownerId)")
    public List<DocumentView> list(
            @RequestParam(required = false) String paymentId,
            @RequestParam(required = false) String ownerKind,
            @RequestParam(required = false) String ownerId
    ) {
        if (paymentId != null && !paymentId.isBlank()) {
            return documents.listByPayment(paymentId);
        }
        return documents.listByOwner(ownerKind, ownerId);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','COMPLIANCE')")
    @Operation(summary = "Mettre à jour le statut (VERIFIED / REJECTED)")
    public DocumentView updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdate body) {
        return documents.updateStatus(id, body.status());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR')")
    @Operation(summary = "Suppression logique + retrait du fichier")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documents.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
