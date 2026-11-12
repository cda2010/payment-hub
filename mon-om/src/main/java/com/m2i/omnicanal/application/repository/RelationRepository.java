package com.m2i.omnicanal.application.repository;



import com.m2i.omnicanal.application.domain.RelationFse;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Relation entity.
 */
@SuppressWarnings("unused")
public interface RelationRepository extends EntityRepository<RelationFse,Long> {

}
