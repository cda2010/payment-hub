package com.m2i.pti.repository;

import com.m2i.pti.domain.CompteAutre;
import jakarta.persistence.LockModeType;

import com.m2i.pti.service.dto.CompteAutreDTO;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.math.BigDecimal;
import jakarta.persistence.QueryHint;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for the CompteAutre entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CompteAutreRepository extends JpaRepository<CompteAutre, Long> {

  //  @Lock(LockModeType.PESSIMISTIC_READ)
  
    @Query(value = "select solde from compte_autre where id=:compteAutreId", nativeQuery = true)
    BigDecimal getSoldeById(@Param("compteAutreId") Long compteAutreId);

    @Query(name = "CompteAutre.findByCompteAutreId", nativeQuery = true)
    CompteAutreDTO findByCompteAutreIdOptimise(@Param("id") Long id);

    @Query(value = "select c.code_fse from compte_autre c where c.id=:caId", nativeQuery = true)
    String getCodeFseById(@Param("caId") Long caId);

    public CompteAutre findCompteAutreById(Long compteId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE compte_autre SET solde = solde + ?2, version=version+1,  last_modified_date=CURRENT_TIMESTAMP WHERE id = ?1", nativeQuery = true)
    int depositXXXX(Long id, BigDecimal montant);

    @Modifying
    @Transactional
    @Query(value = "UPDATE compte_autre SET solde = solde - ?2 ,version=version+1 ,  last_modified_date=CURRENT_TIMESTAMP WHERE id = ?1", nativeQuery = true)
    int withdrawXXXX(Long id, BigDecimal montant);


    @Query(name="CompteAutre.findCompteAutreDTO", nativeQuery=true) 
    CompteAutreDTO getCompteAutre(@Param("valeurCompte") String valeurCompte);


}
