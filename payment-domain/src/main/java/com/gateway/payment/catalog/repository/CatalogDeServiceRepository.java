package com.gateway.payment.catalog.repository;

import com.gateway.payment.catalog.domain.CatalogDeService;
import com.gateway.payment.catalog.domain.CatalogDeServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogDeServiceRepository extends JpaRepository<CatalogDeService, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<CatalogDeService> findByCodeIgnoreCase(String code);

    @Query("""
            SELECT s FROM CatalogDeService s
            JOIN FETCH s.categorie c
            LEFT JOIN FETCH s.currentVersion
            ORDER BY c.ordreAffichage ASC, c.name ASC, s.name ASC
            """)
    List<CatalogDeService> findAllOrderedByCategorieAndName();

    @Query("""
            SELECT s FROM CatalogDeService s
            JOIN FETCH s.categorie c
            LEFT JOIN FETCH s.currentVersion
            WHERE s.status = :status
            ORDER BY c.ordreAffichage ASC, c.name ASC, s.name ASC
            """)
    List<CatalogDeService> findByStatusOrderedByCategorieAndName(CatalogDeServiceStatus status);

    List<CatalogDeService> findByCategorieIdOrderByNameAsc(UUID categorieId);
}
