package com.m2i.pti.repository;

import com.m2i.pti.domain.RepartitionOp;
import com.m2i.pti.domain.enumeration.Acteur;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the RepartitionOp entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RepartitionOpRepository extends JpaRepository<RepartitionOp, Long> {
   RepartitionOp findByActeurAndOperationId(Acteur acteur, Long operationId);
}
