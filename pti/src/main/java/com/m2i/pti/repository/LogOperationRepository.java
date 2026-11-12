package com.m2i.pti.repository;

import com.m2i.pti.domain.LogOperation;
import com.m2i.pti.domain.OperationAudit;
import com.m2i.pti.domain.enumeration.StatutOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.time.ZonedDateTime;
import java.util.List;


/**
 * Spring Data JPA repository for the LogOperation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LogOperationRepository extends JpaRepository<LogOperation, Long> {
//    @Query("SELECT l FROM LogOperation l order by l.dateOperation DESC ")
    Page<LogOperation> findAllByOrderByDateOperationDesc(Pageable pageable);
    Page<LogOperation> findByCodeOperationContainingAndLoginContaining(String codeOperation,String login,Pageable pageable);
    Page<LogOperation> findByCodeOperationContaining(String codeOperation,Pageable pageable);
    Page<LogOperation> findByLoginContaining(String login,Pageable pageable);
    @Query(value = "select l from LogOperation l where " +
            "(l.codeOperation=:codeOperation or :codeOperation is null ) and " +
            "(l.login=:login or :login is null ) and" +
            "(l.dateOperation>=:debut or :debut is null ) and" +
            " (l.dateOperation<=:fin or :fin is null )")
    Page<LogOperation> findByCriteria(@Param("codeOperation") String codeOperation,
                                        @Param("login") String login,
                                        @Param("debut") ZonedDateTime debut,
                                        @Param("fin") ZonedDateTime fin,
                                        Pageable pageable);

    public LogOperation findByCodeOperation(String codeOperation);
}
