package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.application.domain.Etiquette;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Etiquette entity.
 */
@SuppressWarnings("unused")
public interface EtiquetteRepository extends EntityRepository<Etiquette,Long> {

}
