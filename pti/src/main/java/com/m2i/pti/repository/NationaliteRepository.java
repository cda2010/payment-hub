package com.m2i.pti.repository;

import com.m2i.pti.domain.Devise;
import com.m2i.pti.domain.LogOperation;
import com.m2i.pti.domain.Nationalite;
import java.util.List;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 * Spring Data JPA repository for the Nationalite entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NationaliteRepository extends JpaRepository<Nationalite, Long> {
    Optional<Nationalite> findByLibelle(String libelle);

    Optional<Nationalite> findByCode(String code);

    Nationalite findNationaliteByCode(String code);

    @Query(value = "select n.libelle from nationalite n where n.id=:id",nativeQuery = true)
    String getLibelleById(@Param("id") Long id);
    
     @Query(value = "select * from nationalite n ",nativeQuery = true)
List<Nationalite> findAllNationalite();
}
