package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.application.domain.UniteOrg;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the UniteOrganisation entity.
 */
@SuppressWarnings("unused")
public interface UniteOrganisationRepository extends EntityRepository<UniteOrg,Long> {

}
