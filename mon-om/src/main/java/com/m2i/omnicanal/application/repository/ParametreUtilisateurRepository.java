package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.application.domain.ParametreUser;
import java.util.List;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the ParametreUtiliateur entity.
 */
@SuppressWarnings("unused")
public interface ParametreUtilisateurRepository extends EntityRepository<ParametreUser,Long> {
@Query("select p from ParametreUser p where p.interfaceUtilisateur.service.id=?1 ")
    public  List<ParametreUser>    findByService(Long idService);
@Query("select p from ParametreUser p where p.interfaceUtilisateur.id=?1 ")
    public List<ParametreUser> findByInterface(Long idInterface);

}
