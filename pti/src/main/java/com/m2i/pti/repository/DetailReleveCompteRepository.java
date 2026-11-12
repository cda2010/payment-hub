package com.m2i.pti.repository;

import com.m2i.pti.domain.DetailReleveCompte;
import com.m2i.pti.domain.ReleveCompte;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the DetailReleveCompte entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DetailReleveCompteRepository extends JpaRepository<DetailReleveCompte, Long> {

    void deleteByReleve(ReleveCompte releveCompte);
    List<DetailReleveCompte>findAllByReleveId(Long releveId);
}
