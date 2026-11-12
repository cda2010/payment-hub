package com.gateway.payment.orchestration.repository;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.orchestration.domain.InterfaceUtilisateur;
import com.gateway.payment.orchestration.domain.TypeEnvoi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterfaceUtilisateurRepository extends JpaRepository<InterfaceUtilisateur, UUID> {

    List<InterfaceUtilisateur> findByProviderIdOrderByTypeMethodeAsc(UUID providerId);

    Page<InterfaceUtilisateur> findByProvider_Id(UUID providerId, Pageable pageable);

    @Query("""
            SELECT DISTINCT i FROM InterfaceUtilisateur i
            LEFT JOIN FETCH i.parametres
            WHERE i.provider.id = :providerId
            """)
    List<InterfaceUtilisateur> findByProviderIdWithParametres(@Param("providerId") UUID providerId);

    @Query("""
            SELECT DISTINCT i FROM InterfaceUtilisateur i
            LEFT JOIN FETCH i.parametres
            WHERE i.id IN :ids
            """)
    List<InterfaceUtilisateur> findAllByIdInWithParametres(@Param("ids") Collection<UUID> ids);

    @Query("""
            SELECT i FROM InterfaceUtilisateur i
            LEFT JOIN FETCH i.parametres
            WHERE i.id = :id
            """)
    Optional<InterfaceUtilisateur> findByIdWithParametres(@Param("id") UUID id);

    Optional<InterfaceUtilisateur> findByCodeFseAndServiceTypeAndTypeMethodeAndTypeEnvoi(
            String codeFse,
            ServiceTypeCode serviceType,
            String typeMethode,
            TypeEnvoi typeEnvoi
    );

    @Query("""
            SELECT i FROM InterfaceUtilisateur i
            LEFT JOIN FETCH i.parametres
            WHERE i.codeFse = :codeFse
              AND i.typeMethode = :typeMethode
              AND i.typeEnvoi = :typeEnvoi
              AND i.actif = true
            """)
    List<InterfaceUtilisateur> findAllActiveByCodeFseAndTypeMethodeAndTypeEnvoi(
            @Param("codeFse") String codeFse,
            @Param("typeMethode") String typeMethode,
            @Param("typeEnvoi") TypeEnvoi typeEnvoi
    );

    @Query("""
            SELECT i FROM InterfaceUtilisateur i
            LEFT JOIN FETCH i.parametres
            LEFT JOIN FETCH i.provider
            LEFT JOIN FETCH i.providerOperation
            LEFT JOIN FETCH i.providerApi pa
            LEFT JOIN FETCH pa.fseApi
            LEFT JOIN FETCH pa.environment
            WHERE i.codeFse = :codeFse
              AND i.typeMethode = :typeMethode
              AND i.typeEnvoi = :typeEnvoi
              AND i.actif = true
            """)
    List<InterfaceUtilisateur> findAllActiveWithGraphByCodeFseAndTypeMethodeAndTypeEnvoi(
            @Param("codeFse") String codeFse,
            @Param("typeMethode") String typeMethode,
            @Param("typeEnvoi") TypeEnvoi typeEnvoi
    );

    default Optional<InterfaceUtilisateur> findActiveByCodeFseAndTypeMethodeAndTypeEnvoi(
            String codeFse,
            String typeMethode,
            TypeEnvoi typeEnvoi
    ) {
        return findAllActiveWithGraphByCodeFseAndTypeMethodeAndTypeEnvoi(codeFse, typeMethode, typeEnvoi)
                .stream()
                .sorted((a, b) -> {
                    // préférer le serviceType aligné Omnicanal (évite doublons legacy TRANSFERT)
                    int ra = a.getServiceType() == ServiceTypeCode.CONSULTATION_SOLDE ? 0
                            : a.getServiceType() == ServiceTypeCode.TRANSFERT ? 1 : 2;
                    int rb = b.getServiceType() == ServiceTypeCode.CONSULTATION_SOLDE ? 0
                            : b.getServiceType() == ServiceTypeCode.TRANSFERT ? 1 : 2;
                    return Integer.compare(ra, rb);
                })
                .findFirst();
    }

    @Query("""
            SELECT i FROM InterfaceUtilisateur i
            LEFT JOIN FETCH i.parametres
            LEFT JOIN FETCH i.provider
            LEFT JOIN FETCH i.providerOperation
            LEFT JOIN FETCH i.providerApi pa
            LEFT JOIN FETCH pa.fseApi
            LEFT JOIN FETCH pa.environment
            WHERE i.provider.id = :providerId
              AND i.typeMethode = :typeMethode
              AND i.typeEnvoi = :typeEnvoi
              AND i.actif = true
            """)
    List<InterfaceUtilisateur> findAllActiveByProviderIdAndTypeMethodeAndTypeEnvoi(
            @Param("providerId") UUID providerId,
            @Param("typeMethode") String typeMethode,
            @Param("typeEnvoi") TypeEnvoi typeEnvoi
    );

    default Optional<InterfaceUtilisateur> findActiveByProviderIdAndTypeMethodeAndTypeEnvoi(
            UUID providerId,
            String typeMethode,
            TypeEnvoi typeEnvoi
    ) {
        return findAllActiveByProviderIdAndTypeMethodeAndTypeEnvoi(providerId, typeMethode, typeEnvoi)
                .stream()
                .findFirst();
    }
}
