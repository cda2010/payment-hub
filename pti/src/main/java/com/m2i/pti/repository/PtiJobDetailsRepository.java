package com.m2i.pti.repository;

import com.m2i.pti.domain.PtiJobDetails;
import java.util.List;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the PtiJobDetails entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PtiJobDetailsRepository extends JpaRepository<PtiJobDetails, Long> {
@Query("select p from PtiJobDetails p where UPPER(p.jobName)=?1 ")
    public PtiJobDetails findByJobName(String jobName);
@Query("select p from PtiJobDetails p where p.estActif=true")
    public List<PtiJobDetails> findAllActif();

}
