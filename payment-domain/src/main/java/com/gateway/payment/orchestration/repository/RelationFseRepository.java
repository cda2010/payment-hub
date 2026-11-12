package com.gateway.payment.orchestration.repository;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.orchestration.domain.CompteFse;
import com.gateway.payment.orchestration.domain.RelationFse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RelationFseRepository extends JpaRepository<RelationFse, UUID> {

    @Query("""
            SELECT r FROM RelationFse r
            WHERE r.fseSource = :fseSource
              AND r.fseDestination = :fseDest
              AND (r.serviceType IS NULL OR r.serviceType = :serviceType)
            """)
    Optional<RelationFse> findRelation(String fseSource, String fseDest, ServiceTypeCode serviceType);

    @Query("""
            SELECT r FROM RelationFse r
            ORDER BY r.fseSource, r.fseDestination, r.serviceType
            """)
    List<RelationFse> findAllOrdered();

    boolean existsByFseSourceAndFseDestinationAndServiceType(
            String fseSource,
            String fseDestination,
            ServiceTypeCode serviceType
    );
}
