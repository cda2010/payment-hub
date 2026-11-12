package com.gateway.payment.content.repository;

import com.gateway.payment.content.domain.ContentCollection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContentCollectionRepository extends JpaRepository<ContentCollection, UUID> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = "locales")
    Optional<ContentCollection> findByCode(String code);

    @EntityGraph(attributePaths = "locales")
    @Query("SELECT c FROM ContentCollection c ORDER BY c.priority DESC, c.code ASC")
    List<ContentCollection> findAllWithLocales();

    @EntityGraph(attributePaths = "locales")
    @Query("SELECT c FROM ContentCollection c WHERE c.id = :id")
    Optional<ContentCollection> findByIdWithLocales(UUID id);
}
