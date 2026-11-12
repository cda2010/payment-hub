package com.m2i.pti.repository;

import com.m2i.pti.domain.LigneMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Spring Data JPA repository for the LigneMessage entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LigneMessageRepository extends JpaRepository<LigneMessage, Long> {
    public List<LigneMessage> findByChargementMessageId(Long chargementMessageId);

    @Transactional
    @Modifying
    @Query("delete  from LigneMessage l where l.chargementMessage.id=?1")
    void deleteLigne( Long id);

    Integer countByChargementMessageId(Long chargementMessageId);

    @Query("select l from LigneMessage l where l.chargementMessage.id=?1 order by l.id desc")
    Page<LigneMessage> findAllPageByMessage(Long chargementId,Pageable pageable);
}
