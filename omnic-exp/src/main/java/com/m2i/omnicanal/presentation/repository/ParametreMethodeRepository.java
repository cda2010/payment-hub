package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.presentation.domain.ParametreMethod;
import java.util.List;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the ParametreMethode entity.
 */
@SuppressWarnings("unused")
public interface ParametreMethodeRepository extends EntityRepository<ParametreMethod,Long> {
@Query(value="select * from parametre_methode p  where p.methode_id=?1",nativeQuery=true)
    public List<ParametreMethod> findByMethode(Long idmethode);
@Query(value="select * from parametre_methode p , methode m where p.methode_id= m.id and m.code_methode=?1",nativeQuery=true)
    public List<ParametreMethod> findByCodeMethode(String codeMethode);


}
