package com.m2i.omnicanal.application.repository;



import com.m2i.omnicanal.application.domain.LigneOper;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the LigneOperation entity.
 */
@SuppressWarnings("unused")
public interface LigneOperationRepository extends EntityRepository<LigneOper,Long> {
@Query("select COUNT(op.id)  from LigneOper op where op.operation.id=?1 and op.execute=1 and (op.codereponse!='0' or op.codereponse is null)")
    public Integer findLigneNotOk(Long id);
@Query("select COUNT(op.id)  from LigneOper op where op.operation.id=?1 and op.execute=1 and (op.codereponse!='0' or op.codereponse is null)")
    public Integer findLigneExecute(Long id);
@Query("select op  from LigneOper op where op.operation.id=?1 ")
    public List<LigneOper> findByOperationId(Long id);

}
