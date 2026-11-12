package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.application.domain.EtiquetteMethode;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Etiquette entity.
 */
@SuppressWarnings("unused")
public interface EtiquetteMethodeRepository extends EntityRepository<EtiquetteMethode,Long> {

}
