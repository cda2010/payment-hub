package com.m2i.pti.repository;

import com.m2i.pti.domain.Commission;
import com.m2i.pti.domain.PersonnePhysique;
import com.m2i.pti.service.dto.PersonneDTO;
import com.m2i.pti.service.dto.PersonnePhysiqueDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;


/**
 * Spring Data JPA repository for the PersonnePhysique entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PersonnePhysiqueRepository extends JpaRepository<PersonnePhysique, Long> {
    @Query(value = "select p from PersonnePhysique p where (p.nomClient=:nomClient or :nomClient is null) and (p.prenomClient=:prenomClient or :prenomClient is null) and (p.dateNaissance=:dateNaissance or cast(:dateNaissance  as timestamp) is null) ")
    List<PersonnePhysique> findPersonnePhysiqueByMultiCriteria(@Param("nomClient") String nomClient, @Param("prenomClient") String prenomClient, @Param("dateNaissance") LocalDate dateNaissance);

    @Query(name = "PersonnePhysique.findByIdReport",nativeQuery = true)
    PersonnePhysiqueDTO findByIdReport(@Param("ppId") Long ppId);

    @Query(name = "PersonnePhysique.findByIdClient", nativeQuery = true)
    PersonnePhysiqueDTO findPersonneByIdClient(@Param("ppId") Long ppId);

}
