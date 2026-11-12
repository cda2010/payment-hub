package com.gateway.payment.documents.repository;

import com.gateway.payment.documents.domain.StoredDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoredDocumentRepository extends JpaRepository<StoredDocument, UUID> {

    Optional<StoredDocument> findByIdAndDeletedAtIsNull(UUID id);

    List<StoredDocument> findByPaymentIdAndDeletedAtIsNullOrderByCreatedAtAsc(String paymentId);

    List<StoredDocument> findByOwnerKindAndOwnerIdAndDeletedAtIsNullOrderByCreatedAtAsc(
            String ownerKind, String ownerId
    );

    List<StoredDocument> findByTenantIdAndCodeAndDeletedAtIsNullOrderByCreatedAtDesc(
            String tenantId, String code
    );
}
