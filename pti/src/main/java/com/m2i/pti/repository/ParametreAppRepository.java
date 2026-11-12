package com.m2i.pti.repository;

import com.m2i.pti.domain.ParametreApp;
import com.m2i.pti.domain.enumeration.TypeParametre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the ParametreApp entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ParametreAppRepository extends JpaRepository<ParametreApp, Long> {
    ParametreApp findByTypeAndNom(TypeParametre typeParametre, String nom);
    
    @Query("select p from ParametreApp p where p.nom=?1 and p.codeFse=?2 ")
     ParametreApp findByTypeAndNomAndCodeFse( String nom,String codeFse);
    @Query(value = "select p from ParametreApp p order by p.id desc ")
    Page<ParametreApp> findAllOrderbyIdDesc(Pageable pageable);
}
