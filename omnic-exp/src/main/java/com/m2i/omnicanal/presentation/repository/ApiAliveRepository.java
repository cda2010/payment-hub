package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.presentation.domain.ApiAlive;
import com.m2i.omnicanal.presentation.domain.ApplicationApi;
import com.m2i.omnicanal.presentation.domain.HistAppel;
import java.time.ZonedDateTime;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Api entity.
 */
@SuppressWarnings("unused")
public interface ApiAliveRepository extends EntityRepository<ApiAlive,Long> {
      @Query("select a from ApiAlive a where a.dateRequest <= ?1")
    public List<ApiAlive> findAllDateRequestBefore(ZonedDateTime minusDays);
}
