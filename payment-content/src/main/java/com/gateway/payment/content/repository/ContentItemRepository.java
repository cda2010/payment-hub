package com.gateway.payment.content.repository;

import com.gateway.payment.content.domain.ContentItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContentItemRepository extends JpaRepository<ContentItem, UUID> {

    Optional<ContentItem> findByCode(String code);

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = "locales")
    @Query("""
            SELECT i FROM ContentItem i
            WHERE i.slot = :slot
              AND i.enabled = TRUE
              AND (i.startsAt IS NULL OR i.startsAt <= :now)
              AND (i.endsAt IS NULL OR i.endsAt >= :now)
            ORDER BY i.priority DESC, i.code ASC
            """)
    List<ContentItem> findPublished(@Param("slot") String slot, @Param("now") Instant now);

    @EntityGraph(attributePaths = "locales")
    @Query("SELECT i FROM ContentItem i WHERE (:slot IS NULL OR i.slot = :slot) ORDER BY i.slot ASC, i.priority DESC, i.code ASC")
    List<ContentItem> findAllForAdmin(@Param("slot") String slot);

    @EntityGraph(attributePaths = "locales")
    @Query("SELECT i FROM ContentItem i WHERE i.id = :id")
    Optional<ContentItem> findByIdWithLocales(@Param("id") UUID id);
}
