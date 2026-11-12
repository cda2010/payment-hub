package com.gateway.payment.documents.service;

import com.gateway.payment.documents.api.dto.DocumentDtos.DocumentView;
import com.gateway.payment.documents.api.dto.DocumentDtos.UploadMeta;
import com.gateway.payment.documents.domain.DocumentStatus;
import com.gateway.payment.documents.domain.StoredDocument;
import com.gateway.payment.documents.repository.StoredDocumentRepository;
import com.gateway.payment.documents.service.DocumentFileStorage.StoredFile;
import com.gateway.payment.inbox.kafka.PartyActionPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final StoredDocumentRepository repository;
    private final DocumentFileStorage storage;
    private final PartyActionPublisher actions;

    public DocumentService(
            StoredDocumentRepository repository,
            DocumentFileStorage storage,
            PartyActionPublisher actions
    ) {
        this.repository = repository;
        this.storage = storage;
        this.actions = actions;
    }

    @Transactional
    public DocumentView upload(MultipartFile file, UploadMeta meta, String createdBy) {
        StoredFile stored = storage.store(file);
        StoredDocument doc = new StoredDocument();
        doc.setTenantId(meta.tenantId().trim());
        doc.setCode(meta.code().trim().toUpperCase(Locale.ROOT));
        doc.setOwnerKind(meta.ownerKind().trim().toUpperCase(Locale.ROOT));
        doc.setOwnerId(blankToNull(meta.ownerId()));
        doc.setPaymentId(blankToNull(meta.paymentId()));
        doc.setOriginalName(StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename().trim()
                : stored.storageKey());
        doc.setContentType(stored.contentType());
        doc.setSizeBytes(stored.sizeBytes());
        doc.setStorageKey(stored.storageKey());
        doc.setChecksumSha256(stored.checksumSha256());
        doc.setStatus(DocumentStatus.UPLOADED);
        doc.setCreatedBy(blankToNull(createdBy));
        repository.save(doc);
        signalDocument(doc, false);
        return toView(doc);
    }

    @Transactional(readOnly = true)
    public DocumentView get(UUID id) {
        return toView(requireActive(id));
    }

    @Transactional(readOnly = true)
    public List<DocumentView> listByPayment(String paymentId) {
        if (!StringUtils.hasText(paymentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "paymentId requis");
        }
        return repository.findByPaymentIdAndDeletedAtIsNullOrderByCreatedAtAsc(paymentId.trim())
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentView> listByOwner(String ownerKind, String ownerId) {
        if (!StringUtils.hasText(ownerKind) || !StringUtils.hasText(ownerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ownerKind et ownerId requis");
        }
        return repository.findByOwnerKindAndOwnerIdAndDeletedAtIsNullOrderByCreatedAtAsc(
                        ownerKind.trim().toUpperCase(Locale.ROOT),
                        ownerId.trim()
                )
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource loadFile(UUID id) {
        StoredDocument doc = requireActive(id);
        return storage.load(doc.getStorageKey());
    }

    @Transactional(readOnly = true)
    public MediaType mediaType(UUID id) {
        StoredDocument doc = requireActive(id);
        try {
            return MediaType.parseMediaType(doc.getContentType());
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @Transactional(readOnly = true)
    public String originalName(UUID id) {
        return requireActive(id).getOriginalName();
    }

    @Transactional
    public DocumentView updateStatus(UUID id, String statusRaw) {
        StoredDocument doc = requireActive(id);
        DocumentStatus status;
        try {
            status = DocumentStatus.valueOf(statusRaw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut invalide");
        }
        if (status == DocumentStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utiliser DELETE pour supprimer");
        }
        doc.setStatus(status);
        repository.save(doc);
        signalDocument(doc, status == DocumentStatus.REJECTED);
        return toView(doc);
    }

    @Transactional
    public void softDelete(UUID id) {
        StoredDocument doc = requireActive(id);
        doc.setStatus(DocumentStatus.DELETED);
        doc.setDeletedAt(Instant.now());
        repository.save(doc);
        storage.deleteQuietly(doc.getStorageKey());
    }

    private StoredDocument requireActive(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
    }

    private DocumentView toView(StoredDocument doc) {
        return DocumentView.from(doc, storage.downloadUrl(doc.getId()));
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    /**
     * DOC_REQUIRED appartient au coffre documentaire.
     * Un refus ouvre l'action. Un dépôt ou une validation la clôt pour le paiement lié.
     */
    private void signalDocument(StoredDocument doc, boolean rejected) {
        UUID partyId = partyId(doc);
        if (partyId == null) {
            return;
        }
        String paymentId = blankToNull(doc.getPaymentId());
        try {
            if (rejected) {
                actions.open(
                        partyId,
                        "DOC_REQUIRED",
                        paymentId == null ? null : "payment",
                        paymentId,
                        "Pièce refusée : " + doc.getCode(),
                        Instant.now().plus(7, ChronoUnit.DAYS));
            } else if (paymentId != null) {
                actions.complete(partyId, "DOC_REQUIRED", "payment", paymentId);
            }
        } catch (Exception e) {
            log.warn("Échec publication DOC_REQUIRED pour {} : {}", partyId, e.getMessage());
        }
    }

    private static UUID partyId(StoredDocument doc) {
        if (doc.getOwnerId() == null || !"PARTY".equalsIgnoreCase(doc.getOwnerKind())) {
            return null;
        }
        try {
            return UUID.fromString(doc.getOwnerId().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
