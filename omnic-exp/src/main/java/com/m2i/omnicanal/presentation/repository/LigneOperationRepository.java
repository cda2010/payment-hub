package com.m2i.omnicanal.presentation.repository;



import com.m2i.omnicanal.model.LigneOperation;
import com.m2i.omnicanal.presentation.domain.LigneOper;
import com.m2i.omnicanal.presentation.domain.Oper;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the LigneOperation entity.
 */
@SuppressWarnings("unused")
public interface LigneOperationRepository extends EntityRepository<LigneOper,Long> {
@Query("select COUNT(op.id)  from LigneOper op where op.operation.id=?1 and ( op.execute=1 or op.execute=0 ) and (op.codereponse!='0' or op.codereponse is null)")
    public Integer findLigneNotOk(Long id);
@Query("select COUNT(op.id)  from LigneOper op where op.operation.id=?1 and op.execute=1 and (op.codereponse='0')")
    public Integer findLigneExecute(Long id);
@Query("select op from LigneOper op where op.idLgSchema=?1 and op.operation.id=?2")
    public LigneOper findByIdSchemaAndOperation(Long idSchema, Long trnId);
    
    @Query("select op from LigneOper op where op.id=?1 and op.operation.id=?2")
    public LigneOper findByIdLigneAndOperation(Long idLigne, Long trnId);
    
    
        
    @Query("select op from LigneOper op where op.operation.id=?1")
    public List<LigneOper>  findByOperationId( Long trnId);
    
@Query("select op  from LigneOper op where  op.codereponse <> '0' or op.codereponse is null")
    public List<LigneOper> findReversalTransaction();
    
    
    
    @Query("select op  from LigneOper op where   op.operation.id=?1 and op.revers!=1 and op.execute=1 and op.codereponse = '1'")
    public List<LigneOper> findReversalTransactionAllOperId(Long opId);
    
    
    
    @Query("select op  from LigneOper op where   op.operation.id=?1 and op.revers!=1 and op.execute=1 and op.codereponse = '0'")
    public List<LigneOper> findReversalTransactionOperId(Long opId);
  @Query("select DISTINCT(op.operation)  from LigneOper op where  (op.codereponse!='0') and op.revers!=1 and op.execute=1")
    public List<Oper> findDistinctTransaction();
   @Query("select DISTINCT(op.operation)  from LigneOper op where  op.execute=0")
    public List<Oper> findDistinctTransactionDiffere();  
    
       @Query("select op  from LigneOper op where   op.operation.id=?1  and op.execute=0")
    public List<LigneOper> findDiffereTransactionOperId(Long opId);
    
    @Query("select COUNT(op.operation)  from LigneOper op where   op.operation.id=?1 and op.revers!=1 and op.execute=1 and op.codereponse = '0' and op.operation.ack=0")
    public Integer findCountExcuteTransactionOperId(Long opId);
    
      @Query("select COUNT(op.operation)  from LigneOper op where   op.operation.id=?1  and op.execute=1 and op.codereponse <> '0'")
    public Integer findCountNonExcuteTransactionOperId(Long opId);
     @Query("select COUNT(op.operation)  from LigneOper op where  op.execute=0 and op.operation.id=?1 and op.codereponse is null" )
    public Integer findCountTransactionDiffere(Long opId);  
    
    
     @Query("select op  from LigneOper op where   op.operation.id=?1 and op.revers!=1  and op.execute=1 and op.istraite=0 and op.codereponse = '0' and op.operation.ack=0")
    public List<LigneOper> findTransactionOperId(Long opId);
}
