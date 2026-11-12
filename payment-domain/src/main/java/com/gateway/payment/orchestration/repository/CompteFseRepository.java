package com.gateway.payment.orchestration.repository;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.orchestration.domain.CompteFse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompteFseRepository extends JpaRepository<CompteFse, UUID> {

    /**
     * Inter-FSE — Omnicanal
     * {@code findCompteFse(etiquette, agence, typeServiceId, relationId, fseTeneur)}.
     * {@code agenceCode} null/blank = toutes agences.
     */
    @Query("""
            SELECT c FROM CompteFse c
            WHERE c.typeCompte.codeParametre = :etiquette
              AND (c.serviceType IS NULL OR c.serviceType = :serviceType)
              AND (c.relation IS NULL OR c.relation.id = :relationId)
              AND c.fseTeneur = :fseTeneur
              AND (
                    :agenceCode IS NULL OR :agenceCode = ''
                    OR c.agenceCode = :agenceCode
                    OR c.codeAgence = :agenceCode
                  )
            ORDER BY c.omniId ASC NULLS LAST, c.createdAt ASC
            """)
    List<CompteFse> findCompteFseCandidates(
            @Param("etiquette") String etiquette,
            @Param("serviceType") ServiceTypeCode serviceType,
            @Param("relationId") UUID relationId,
            @Param("fseTeneur") String fseTeneur,
            @Param("agenceCode") String agenceCode
    );

    default Optional<CompteFse> findCompteFse(
            String etiquette,
            ServiceTypeCode serviceType,
            UUID relationId,
            String fseTeneur
    ) {
        return findCompteFse(etiquette, serviceType, relationId, fseTeneur, null);
    }

    default Optional<CompteFse> findCompteFse(
            String etiquette,
            ServiceTypeCode serviceType,
            UUID relationId,
            String fseTeneur,
            String agenceCode
    ) {
        return findCompteFseCandidates(etiquette, serviceType, relationId, fseTeneur, agenceCode)
                .stream()
                .findFirst();
    }

    /**
     * Same-FSE — Omnicanal
     * {@code findCompteFse(agence, codeFseTitulaire, codeParametre, codeUoTitulaire, typeServiceId)}.
     */
    @Query("""
            SELECT c FROM CompteFse c
            WHERE c.typeCompte.codeParametre = :etiquette
              AND c.fseTitulaire = :fseTitulaire
              AND (c.serviceType IS NULL OR c.serviceType = :serviceType)
              AND (
                    :agenceCode IS NULL OR :agenceCode = ''
                    OR c.agenceCode = :agenceCode
                    OR c.codeAgence = :agenceCode
                  )
              AND (
                    :codeUoTitulaire IS NULL OR :codeUoTitulaire = ''
                    OR c.codeUoTitulaire IS NULL
                    OR c.codeUoTitulaire = :codeUoTitulaire
                  )
            ORDER BY c.omniId ASC NULLS LAST, c.createdAt ASC
            """)
    List<CompteFse> findCompteFseSameFseCandidates(
            @Param("agenceCode") String agenceCode,
            @Param("fseTitulaire") String fseTitulaire,
            @Param("etiquette") String etiquette,
            @Param("codeUoTitulaire") String codeUoTitulaire,
            @Param("serviceType") ServiceTypeCode serviceType
    );

    default Optional<CompteFse> findCompteFseSameFse(
            String agenceCode,
            String fseTitulaire,
            String etiquette,
            String codeUoTitulaire,
            ServiceTypeCode serviceType
    ) {
        return findCompteFseSameFseCandidates(agenceCode, fseTitulaire, etiquette, codeUoTitulaire, serviceType)
                .stream()
                .findFirst();
    }

    /** Omnicanal {@code findCompteByEtiquetteAndFse(codeFseTitulaire, etiquette)}. */
    @Query("""
            SELECT c FROM CompteFse c
            WHERE c.fseTitulaire = :fseTitulaire
              AND c.typeCompte.codeParametre = :etiquette
            ORDER BY c.omniId ASC NULLS LAST, c.createdAt ASC
            """)
    List<CompteFse> findByEtiquetteAndFseTitulaireCandidates(
            @Param("fseTitulaire") String fseTitulaire,
            @Param("etiquette") String etiquette
    );

    default Optional<CompteFse> findCompteByEtiquetteAndFse(String fseTitulaire, String etiquette) {
        return findByEtiquetteAndFseTitulaireCandidates(fseTitulaire, etiquette).stream().findFirst();
    }

    @Query("""
            SELECT c FROM CompteFse c
            JOIN FETCH c.typeCompte
            WHERE c.relation.id = :relationId
            ORDER BY c.typeCompte.codeParametre
            """)
    List<CompteFse> findByRelationIdWithType(UUID relationId);

    @Query("""
            SELECT c FROM CompteFse c
            JOIN FETCH c.typeCompte
            LEFT JOIN FETCH c.relation
            """)
    List<CompteFse> findAllWithTypeAndRelation();

    void deleteByRelation_Id(UUID relationId);
}
