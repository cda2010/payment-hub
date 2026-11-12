package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.presentation.domain.ParametreUser;
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
@Query("select u from ParametreUser u where u.interfaceUtilisateur.service.fournisseurService.codeFse=?1 and u.interfaceUtilisateur.service.typeService.id=?2")
    public List<ParametreUser> findByServiceFse(String fse, Long typeService);

}
