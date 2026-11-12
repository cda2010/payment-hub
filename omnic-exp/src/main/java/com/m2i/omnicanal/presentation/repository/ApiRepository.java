package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.presentation.domain.ApplicationApi;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Api entity.
 */
@SuppressWarnings("unused")
public interface ApiRepository extends EntityRepository<ApplicationApi,Long> {
      
}
