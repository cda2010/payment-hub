package com.gateway.payment.configuration.repository;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.domain.PaymentProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentProviderRepository extends JpaRepository<PaymentProvider, UUID> {
    Optional<PaymentProvider> findByCode(String code);

    @Query("select p from PaymentProvider p left join fetch p.serviceTypes where p.code = :code")
    Optional<PaymentProvider> findByCodeWithServiceTypes(@Param("code") String code);

    List<PaymentProvider> findByActifTrueOrderByOrdreAffichageAsc();

    List<PaymentProvider> findByParticipantKindAndActifTrueOrderByOrdreAffichageAsc(
            com.gateway.payment.configuration.domain.FseParticipantKind participantKind);

    boolean existsByCode(String code);

    Optional<PaymentProvider> findByCodeFse(String codeFse);

    boolean existsByCodeFse(String codeFse);

    @Query("""
            select distinct p from PaymentProvider p
            join p.serviceTypes st
            where p.actif = true and st.code = :serviceType
            order by p.ordreAffichage asc
            """)
    List<PaymentProvider> findActiveByServiceType(@Param("serviceType") ServiceTypeCode serviceType);

    @Query("select p from PaymentProvider p left join fetch p.serviceTypes where p.id = :id")
    Optional<PaymentProvider> findByIdWithServiceTypes(@Param("id") UUID id);

    @Query("select distinct p from PaymentProvider p left join fetch p.serviceTypes order by p.ordreAffichage asc")
    List<PaymentProvider> findAllWithServiceTypes();

    @Query("select distinct p from PaymentProvider p left join fetch p.serviceTypes where p.id in :ids")
    List<PaymentProvider> findAllWithServiceTypesByIdIn(@Param("ids") Collection<UUID> ids);

    @Query("select distinct p from PaymentProvider p left join fetch p.serviceTypes where p.actif = true order by p.ordreAffichage asc")
    List<PaymentProvider> findActiveWithServiceTypes();

    @Query("select p from PaymentProvider p where p.codeFse is not null and p.codeFse <> ''")
    List<PaymentProvider> findByCodeFseIsNotNull();

    @Query("""
            select p from PaymentProvider p
            where lower(p.code) like lower(concat('%', :q, '%'))
               or lower(p.libelle) like lower(concat('%', :q, '%'))
               or lower(coalesce(p.codeFse, '')) like lower(concat('%', :q, '%'))
               or lower(coalesce(p.description, '')) like lower(concat('%', :q, '%'))
            """)
    Page<PaymentProvider> search(@Param("q") String q, Pageable pageable);
}
