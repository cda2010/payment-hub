package com.gateway.payment.documents.api.dto;

import com.gateway.payment.documents.domain.DocumentStatus;
import com.gateway.payment.documents.domain.StoredDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class DocumentDtos {

    private DocumentDtos() {
    }

    public record UploadMeta(
            @NotBlank @Size(max = 64) String tenantId,
            @NotBlank @Size(max = 80) String code,
            @NotBlank @Size(max = 32) String ownerKind,
            @Size(max = 128) String ownerId,
            @Size(max = 128) String paymentId
    ) {
    }

    public record StatusUpdate(
            @NotBlank String status
    ) {
    }

    public record DocumentView(
            UUID id,
            String tenantId,
            String code,
            String ownerKind,
            String ownerId,
            String paymentId,
            String originalName,
            String contentType,
            long sizeBytes,
            String checksumSha256,
            DocumentStatus status,
            String downloadUrl,
            String createdBy,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static DocumentView from(StoredDocument doc, String downloadUrl) {
            return new DocumentView(
                    doc.getId(),
                    doc.getTenantId(),
                    doc.getCode(),
                    doc.getOwnerKind(),
                    doc.getOwnerId(),
                    doc.getPaymentId(),
                    doc.getOriginalName(),
                    doc.getContentType(),
                    doc.getSizeBytes(),
                    doc.getChecksumSha256(),
                    doc.getStatus(),
                    downloadUrl,
                    doc.getCreatedBy(),
                    doc.getCreatedAt(),
                    doc.getUpdatedAt()
            );
        }
    }
}
