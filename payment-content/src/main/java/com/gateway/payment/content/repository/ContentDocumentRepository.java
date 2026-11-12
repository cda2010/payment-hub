package com.gateway.payment.content.repository;

import com.gateway.payment.content.domain.ContentDocument;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContentDocumentRepository extends JpaRepository<ContentDocument, UUID> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"locales", "collection"})
    Optional<ContentDocument> findByCode(String code);

    @EntityGraph(attributePaths = {"locales", "collection"})
    @Query("SELECT d FROM ContentDocument d WHERE d.id = :id")
    Optional<ContentDocument> findByIdWithLocales(@Param("id") UUID id);

    @EntityGraph(attributePaths = "locales")
    @Query("""
            SELECT d FROM ContentDocument d
            WHERE d.collection.code = :collectionCode
            ORDER BY d.priority DESC, d.code ASC
            """)
    List<ContentDocument> findAllByCollectionCode(@Param("collectionCode") String collectionCode);

    @EntityGraph(attributePaths = "locales")
    @Query("""
            SELECT d FROM ContentDocument d
            WHERE d.collection.code = :collectionCode
              AND d.enabled = TRUE
              AND (d.startsAt IS NULL OR d.startsAt <= :now)
              AND (d.endsAt IS NULL OR d.endsAt >= :now)
            ORDER BY d.priority DESC, d.code ASC
            """)
    List<ContentDocument> findPublishedByCollection(
            @Param("collectionCode") String collectionCode,
            @Param("now") Instant now
    );
}
