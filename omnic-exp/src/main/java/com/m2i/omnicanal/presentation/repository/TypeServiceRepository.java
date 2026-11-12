package com.m2i.omnicanal.presentation.repository;
import com.m2i.omnicanal.presentation.domain.TypeServ;
import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Compte entity.
 */
@SuppressWarnings("unused")
public interface TypeServiceRepository extends EntityRepository<TypeServ,Long> {
@Query("select t from TypeServ t where t.codeTypeService=?1")
    public TypeServ findByCodeService(String codeService);


}
