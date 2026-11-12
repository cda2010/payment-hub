package com.m2i.pti.repository;

import com.m2i.pti.domain.Beneficiaire;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.List;


/**
 * Spring Data JPA repository for the Beneficiaire entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BeneficiaireRepository extends JpaRepository<Beneficiaire, Long> {

    Beneficiaire findByPersonneId(Long personneId);

    Beneficiaire findTop1ByPersonneId(Long personneId);
    @Query(value = "select distinct personne_id from beneficiaire where id=CAST (CAST (:beneficiaireId as text) as BIGINT)",nativeQuery = true)
    Long getPersonneIdByBeneficiaireId(@Param("beneficiaireId") Long beneficiaireId);

    @Query(value = "select distinct personne_id from beneficiaire where id=CAST (CAST (:beneficiaire as text) as BIGINT)",nativeQuery = true)
    BigInteger getPersonneId(@Param("beneficiaire") BigInteger beneficiaire);

    @Query("select b from  Beneficiaire b where b.id=?1")

    List<Beneficiaire> findAllByPersonneId(Long personneId);



}
