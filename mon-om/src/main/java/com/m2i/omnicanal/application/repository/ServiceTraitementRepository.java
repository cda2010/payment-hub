package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.application.domain.Service;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the ServiceTraitement entity.
 */
@SuppressWarnings("unused")
public interface ServiceTraitementRepository extends EntityRepository<Service,Long> {

    

}
