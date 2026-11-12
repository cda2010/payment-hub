package com.m2i.pti.repository;

import com.m2i.pti.domain.MotifOperation;
import com.m2i.pti.service.dto.MotifOperationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the MotifOperation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MotifOperationRepository extends JpaRepository<MotifOperation, Long> {
    MotifOperation findByLibelle(String libelle);
    MotifOperation findByCode(String code);
    @Query("SELECT coalesce(max(m.id), 0) FROM MotifOperation m ")
    Long getMaxId();

    @Query(name = "MotifOperation.findByCode", nativeQuery = true)
    MotifOperationDTO findByCodeMotif(@Param("code") String code); 
    
    
}
