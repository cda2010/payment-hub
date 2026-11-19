package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.CatalogueRecompense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogueRecompenseRepository extends JpaRepository<CatalogueRecompense, UUID> {

    List<CatalogueRecompense> findByTenantIdOrderByCodeAsc(String tenantId);

    Optional<CatalogueRecompense> findByTenantIdAndCodeIgnoreCase(String tenantId, String code);
}
