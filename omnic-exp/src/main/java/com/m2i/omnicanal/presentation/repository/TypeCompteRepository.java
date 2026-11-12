package com.m2i.omnicanal.presentation.repository;



import com.m2i.omnicanal.presentation.domain.TypeCpt;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the TypeCompte entity.
 */
@SuppressWarnings("unused")
public interface TypeCompteRepository extends EntityRepository<TypeCpt,Long> {

}
