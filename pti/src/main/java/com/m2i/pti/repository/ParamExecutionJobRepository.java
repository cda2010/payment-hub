package com.m2i.pti.repository;

import com.m2i.pti.domain.ParamExecutionJob;
import java.util.List;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the ParamExecutionJob entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ParamExecutionJobRepository extends JpaRepository<ParamExecutionJob, Long> {


    public List<ParamExecutionJob> findByPtiJobDetailId(Long id);
    @Query("select p from ParamExecutionJob p where p.ptiJobDetail.id=?1")
    List<ParamExecutionJob> queryByPtiJobDetail(Long id);

    @Query("select count(p) from ParamExecutionJob p where p.ptiJobDetail.id=?1")
    Integer countByKey(Long id);

}
