package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.presentation.domain.Etiquette;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Etiquette entity.
 */
@SuppressWarnings("unused")
public interface EtiquetteRepository extends EntityRepository<Etiquette,Long> {

}
