package com.m2i.pti.repository;

import com.m2i.pti.domain.Taxe;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the Taxe entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TaxeRepository extends JpaRepository<Taxe, Long> {
    Boolean existsByCode(String code);
    Boolean existsByNom(String nom);

}
