package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.application.domain.TypeUniteOrg;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the TypeUniteOrganisation entity.
 */
@SuppressWarnings("unused")
public interface TypeUniteOrganisationRepository extends EntityRepository<TypeUniteOrg,Long> {

}
