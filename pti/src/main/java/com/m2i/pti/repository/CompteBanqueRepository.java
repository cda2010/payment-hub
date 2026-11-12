package com.m2i.pti.repository;

import com.m2i.pti.domain.CompteBanque;
import com.m2i.pti.domain.EtiquetteCompte;
import com.m2i.pti.domain.enumeration.EtiquetteCompteCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the CompteBanque entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CompteBanqueRepository extends JpaRepository<CompteBanque, Long> {

    Page<CompteBanque> findByCodeAgenceContaining(String code, Pageable pageable);

    @Query(value = "select c from CompteBanque c where (c.codeAgence=:codeAgence or :codeAgence is null ) "
            + "and (c.codeBanque=:codeBanque or :codeBanque is null ) "
            + "and (c.rib=:rib or :rib is null ) and c.id in (select cb.compteBanque.id from  Compte cb where cb.etiquette=:etiquette and (cb.valeurCompte=:valeur  or :valeur is null )) ")
    List<CompteBanque> finbyCriteria(@Param("codeAgence") String codeAgence, @Param("codeBanque") String codeBanque,
            @Param("rib") String rib, @Param("valeur") String valeur, @Param("etiquette") String etiquetteCompteCode);

    List<CompteBanque> findByCodeAgenceAndCodeFseAndRib(String codeAgence, String codeFse, String rib);

    @Query(value = "select c.code_fse from compte_banque c where c.id=:cbId", nativeQuery = true)
    String getCodeFseById(@Param("cbId") Long cbId);
    @Query(value = "select c from CompteBanque c where c.id=?1")
    CompteBanque findByCompteId(Long id);

}
