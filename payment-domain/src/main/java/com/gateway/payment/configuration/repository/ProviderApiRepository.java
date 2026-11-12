package com.gateway.payment.configuration.repository;

import com.gateway.payment.configuration.domain.ProviderApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderApiRepository extends JpaRepository<ProviderApi, UUID> {
    List<ProviderApi> findByProviderId(UUID providerId);

    Optional<ProviderApi> findByProviderIdAndCode(UUID providerId, String code);

    @Query("""
            SELECT a FROM ProviderApi a
            LEFT JOIN FETCH a.fseApi fa
            LEFT JOIN FETCH fa.environment
            LEFT JOIN FETCH a.environment
            WHERE a.provider.id = :providerId AND a.code = :code
            """)
    Optional<ProviderApi> findByProviderIdAndCodeWithGraph(
            @Param("providerId") UUID providerId,
            @Param("code") String code
    );

    List<ProviderApi> findByOperationId(UUID operationId);

    @Query("""
            SELECT a FROM ProviderApi a
            LEFT JOIN FETCH a.fseApi fa
            LEFT JOIN FETCH fa.environment
            LEFT JOIN FETCH a.environment
            WHERE a.operation.id = :operationId
            """)
    List<ProviderApi> findByOperationIdWithGraph(@Param("operationId") UUID operationId);

    List<ProviderApi> findByFseApiId(UUID fseApiId);

    long countByFseApiId(UUID fseApiId);
}
