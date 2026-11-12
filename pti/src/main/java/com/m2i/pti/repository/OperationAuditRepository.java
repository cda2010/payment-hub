package com.m2i.pti.repository;

import com.m2i.pti.domain.OperationAudit;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.service.dto.OperationAuditDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;


/**
 * Spring Data JPA repository for the OperationAudit entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OperationAuditRepository extends JpaRepository<OperationAudit, Long> {
    OperationAudit findTop1ByOperationIdOrderByDateDesc(Long operationId);
    List<OperationAudit> findByOperationIdAndStatutPrecedentAndStatutApres(Long operationId, StatutOperation statutPrecedent,StatutOperation statutApres);

    @Query(value = "select op from OperationAudit op where (op.statutPrecedent=:statutPrecedent or :statutPrecedent is null ) and " +
            "(op.statutApres=:statutApres or :statutApres is null )")
    Page<OperationAudit> findByCriteria(@Param("statutPrecedent") StatutOperation statutPrecedent,
                                        @Param("statutApres") StatutOperation statutApres,
                                        Pageable pageable);

    @Query(name = "OperationAudit.findAllAuditOperation",nativeQuery = true)
    List<OperationAuditDTO> findByCriteriaOptimiser(@Param("statutPrecedent") StatutOperation statutPrecedent, @Param("statutApres") StatutOperation statutApres);


}
