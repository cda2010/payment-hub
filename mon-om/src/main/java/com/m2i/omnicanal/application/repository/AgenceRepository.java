package com.m2i.omnicanal.application.repository;



import com.m2i.omnicanal.application.domain.AgenceFse;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Spring Data JPA repository for the Agence entity.
 */
@SuppressWarnings("unused")
public interface AgenceRepository extends EntityRepository<AgenceFse,Long> {

    public List<AgenceFse> findByFournisseurServiceId(Long fseId);
@Query("select  a from AgenceFse a where fournisseurService.id=?1")
    public Page<AgenceFse> findAll(Pageable pageable, Long fseId);

}
