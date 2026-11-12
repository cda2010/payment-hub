package com.m2i.pti.repository;

import com.m2i.pti.domain.RepartionFraisEmAcq;
import com.m2i.pti.domain.TypeService;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.Optional;


/**
 * Spring Data JPA repository for the RepartionFraisEmAcq entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RepartionFraisEmAcqRepository extends JpaRepository<RepartionFraisEmAcq, Long> {
    RepartionFraisEmAcq findByTypeService(TypeService typeService);
    boolean existsByTypeServiceId(Long typeServiceId);
    Optional<RepartionFraisEmAcq> findByTypeServiceId(Long typeServiceId);

}
