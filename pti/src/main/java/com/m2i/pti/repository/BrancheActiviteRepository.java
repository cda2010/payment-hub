package com.m2i.pti.repository;

import com.m2i.pti.domain.BrancheActivite;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the BrancheActivite entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BrancheActiviteRepository extends JpaRepository<BrancheActivite, Long> {

}
