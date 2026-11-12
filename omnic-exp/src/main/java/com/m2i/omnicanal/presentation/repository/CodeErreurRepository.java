package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.presentation.domain.ApiAlive;
import com.m2i.omnicanal.presentation.domain.ApplicationApi;
import com.m2i.omnicanal.presentation.domain.CodeErreur;
import com.m2i.omnicanal.presentation.domain.HistAppel;
import java.time.ZonedDateTime;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Api entity.
 */
@SuppressWarnings("unused")
public interface CodeErreurRepository extends EntityRepository<CodeErreur,Long> {
      @Query("select a from CodeErreur a where a.code = ?1")
    public List<CodeErreur> findAllCodeErreur(String code);
}
