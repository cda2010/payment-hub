package com.m2i.omnicanal.presentation.repository;



import com.m2i.omnicanal.presentation.domain.PaysFse;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Pays entity.
 */
@SuppressWarnings("unused")
public interface PaysRepository extends EntityRepository<PaysFse,Long> {

}
