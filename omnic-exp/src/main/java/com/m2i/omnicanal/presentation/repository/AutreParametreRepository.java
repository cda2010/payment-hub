package com.m2i.omnicanal.presentation.repository;


import com.m2i.omnicanal.model.AutreParametre;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the AutreParametre entity.
 */
@SuppressWarnings("unused")
public interface AutreParametreRepository extends EntityRepository<AutreParametre,Long> {
@Query("select a from AutreParametre a where a.fournisseurService.codeFse=?1")
public List<AutreParametre> findAllFournisseurService(String fseCode);

}
