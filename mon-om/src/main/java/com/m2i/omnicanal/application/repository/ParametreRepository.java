package com.m2i.omnicanal.application.repository;


import com.m2i.omnicanal.application.domain.Parameters;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Parametre entity.
 */
@SuppressWarnings("unused")
public interface ParametreRepository extends EntityRepository<Parameters,Long> {

}
