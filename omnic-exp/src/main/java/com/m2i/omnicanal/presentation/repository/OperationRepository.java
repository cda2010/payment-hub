package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.model.Operation;
import com.m2i.omnicanal.presentation.domain.Oper;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Operation entity.
 */
@SuppressWarnings("unused")
public interface OperationRepository extends EntityRepository<Oper, Long> {

    @Query("select o from Operation o order by o.dateOperation desc")
    public List<Oper> findAllDateOperation();

    @Query("select count(o) from Operation o where o.reference=?1")
    public Long findByReference(String codeOperation);
    
      @Query("select o from Operation o where o.reference=?1")
    public Oper findByReferenceEx(String codeOperation);

    @Query("select o from Operation o where o.codeOperation=?1")
    public Oper findByCodeOperation(String codeOperation);

    @Query("select o from Operation o where o.ack=0 and o.fseCodeSource=?1 order by o.dateOperation desc")
    public List<Oper> findAllBycodeFseOrderByDateOperation(String codefse);

}
