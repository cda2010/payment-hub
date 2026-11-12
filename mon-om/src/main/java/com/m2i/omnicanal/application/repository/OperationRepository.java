package com.m2i.omnicanal.application.repository;




import com.m2i.omnicanal.application.domain.Oper;
import com.m2i.omnicanal.application.service.dto.OperationDTO;

import com.m2i.omnicanal.model.Operation;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Operation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OperationRepository extends EntityRepository<Oper,Long> {
    
//@Query("select o from Operation o order by o.dateOperation desc" )
    @Query(name="Operation.findAll", nativeQuery=true)    
    public List<OperationDTO> findAllDateOperation();

//    @Query("select o from Operation o where ((o.fseCodeSource = coalesce(:pfseSrc,o.fseCodeSource)) AND" +
//        " (:pdebut IS NULL OR o.dateOperation >= :pdebut) AND (:pfin IS NULL OR o.dateOperation <= :pfin) AND" +
//        " (:pfseDest IS NULL OR o.fseCodeDest = :pfseDest) AND (:ptypeServiceId IS NULL OR o.typeService.id =:ptypeServiceId) AND" +
//        " (:pnumero IS NULL OR o.codeOperation = :pnumero)) order by o.dateOperation desc" )


    
    
  /*  
 @Query("select o from Operation o where ((o.fseCodeSource = coalesce(:pfseSrc,o.fseCodeSource)) AND" +
        "(o.dateOperation >= coalesce(:pdebut,o.dateOperation)) AND (o.dateOperation <= coalesce(:pfin,o.dateOperation)) AND" +
        " (o.fseCodeDest = coalesce(:pfseDest,o.fseCodeDest)) AND (o.typeService.id = coalesce(:ptypeServiceId,o.typeService.id)) AND" +
        " (o.codeOperation = coalesce(:pnumero,o.codeOperation))) order by o.dateOperation desc" )*/
            
    @Query(name="Operation.findOperation", nativeQuery=true)       
    List<OperationDTO> rechercheOperation(@Param("ptypeServiceId") Long ptypeServiceId,
                                        @Param("pnumero") String pnumero,
                                        @Param("pfseSrc") String pfseSrc,
                                        @Param("pfseDest") String pfseDest,
                                        @Param("pdebut") ZonedDateTime pdebut,
                                        @Param("pfin") ZonedDateTime pfin);

 @Query(name="Operation.findTop10Operation", nativeQuery=true)    
    public List<OperationDTO> findTop10Operation();
}
