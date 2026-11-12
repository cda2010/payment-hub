package com.gateway.payment.catalog.repository;

import com.gateway.payment.catalog.domain.CategorieService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategorieServiceRepository extends JpaRepository<CategorieService, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<CategorieService> findByCodeIgnoreCase(String code);

    List<CategorieService> findAllByOrderByOrdreAffichageAscNameAsc();

    List<CategorieService> findByActifTrueOrderByOrdreAffichageAscNameAsc();
}
