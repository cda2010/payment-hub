package com.m2i.pti.repository;

import com.m2i.pti.domain.ChargementCompte;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;


/**
 * Spring Data JPA repository for the ChargementCompte entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChargementCompteRepository extends JpaRepository<ChargementCompte, Long> {
    @Query("select count(c)   from ChargementCompte c where c.hashFichier=?1")
    public Integer findByHashFichier(String hash);
    @Query("select count(c)  from ChargementCompte c where c.nomFichier=?1  ")
    public Integer findByNomFichier(String nomFichier);
@Query("select c  from ChargementCompte c order by c.reference desc ,c.dateChargement desc")
    public List<ChargementCompte> findAllOrderByDateChargement();

    @Transactional
    @Query("select c  from ChargementCompte c order by c.reference desc ,c.dateChargement desc")
    public Page<ChargementCompte> findAllOrderByDateChargementPage(Pageable pageable);
}
