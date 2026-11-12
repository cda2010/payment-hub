package com.gateway.payment.billing.repository;

import com.gateway.payment.billing.domain.Biller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillerRepository extends JpaRepository<Biller, UUID> {
    Optional<Biller> findByCodeIgnoreCase(String code);

    List<Biller> findByActifTrueOrderByOrdreAffichageAscNameAsc();

    List<Biller> findAllByOrderByOrdreAffichageAscNameAsc();

    boolean existsByCodeIgnoreCase(String code);

    @Query("""
            SELECT b.categoryCode, COUNT(b)
            FROM Biller b
            WHERE b.actif = true
            GROUP BY b.categoryCode
            """)
    List<Object[]> countActiveByCategory();

    /**
     * Filtre optionnel via chaînes vides (jamais null) — évite {@code upper(bytea)}
     * côté PostgreSQL quand Hibernate bind un null non typé.
     */
    @Query("""
            SELECT b FROM Biller b
            WHERE b.actif = true
              AND (:category = '' OR UPPER(COALESCE(b.categoryCode, 'OTHER')) = UPPER(:category))
              AND (
                    :q = ''
                    OR LOWER(b.name) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(b.code) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(COALESCE(b.description, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                  )
            ORDER BY b.ordreAffichage ASC NULLS LAST, b.name ASC
            """)
    Page<Biller> searchActive(
            @Param("category") String category,
            @Param("q") String q,
            Pageable pageable
    );
}
