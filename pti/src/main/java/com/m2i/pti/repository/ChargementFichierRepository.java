package com.m2i.pti.repository;

import com.m2i.pti.domain.ChargementFichier;
import com.m2i.pti.domain.ChargementTransfert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChargementFichierRepository extends JpaRepository<ChargementFichier,Long> {

    @Query("select count(c)   from ChargementFichier c where c.hashFichier=?1")
    public Integer findByHashFichier(String hash);
    @Query("select count(c)  from ChargementFichier c where c.nomFichier=?1  ")
    public Integer findByNomFichier(String nomFichier);

    @Query("select c from ChargementFichier c order by c.id desc")
    public List<ChargementFichier> findAllOrderByDateDesc();

    @Query("select c from ChargementFichier c where c.uniteOrganisation.id=?1 order by c.id desc")
    Page<ChargementFichier> findAllByOrganisationIdPage(Long organisationId, Pageable pageable);

}
