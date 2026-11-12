package com.gateway.payment.catalog.repository;

import com.gateway.payment.catalog.domain.CatalogDeServiceVersion;
import com.gateway.payment.catalog.domain.CatalogDeServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogDeServiceVersionRepository extends JpaRepository<CatalogDeServiceVersion, UUID> {

    List<CatalogDeServiceVersion> findByCatalogDeServiceIdOrderByCreatedAtAsc(UUID catalogDeServiceId);

    Optional<CatalogDeServiceVersion> findByCatalogDeServiceIdAndVersion(UUID catalogDeServiceId, String version);

    boolean existsByCatalogDeServiceIdAndVersion(UUID catalogDeServiceId, String version);

    Optional<CatalogDeServiceVersion> findFirstByCatalogDeServiceIdAndStatusOrderByEffectiveFromDesc(
            UUID catalogDeServiceId,
            CatalogDeServiceStatus status
    );
}
