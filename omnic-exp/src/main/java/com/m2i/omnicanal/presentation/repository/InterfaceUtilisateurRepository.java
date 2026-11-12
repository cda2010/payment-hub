package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.model.enumeration.TypeEnvoi;
import com.m2i.omnicanal.model.enumeration.TypeMethode;
import com.m2i.omnicanal.presentation.domain.InterfaceUser;
import java.util.List;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the InterfaceUtilisateur entity.
 */
@SuppressWarnings("unused")
public interface InterfaceUtilisateurRepository extends EntityRepository<InterfaceUser,Long> {
@Query("select i from InterfaceUser i where i.service.codeService=?1")
    public InterfaceUser findByInterService(String codeService);
@Query("select i from InterfaceUser i where i.service.fournisseurService.codeFse=?1 and i.service.typeService.id=?2")
    public List<InterfaceUser> findByServiceFse(String fse, Long typeService);
@Query("select i from InterfaceUser i where i.service.fournisseurService.codeFse=?1 and i.service.typeService.id=?2")
    public InterfaceUser findParTypeServiceFse(String fse, Long typeService);
    @Query("select i from InterfaceUser i where i.fournisseurService.codeFse=?1 and i.typeMethode=?2")
    public InterfaceUser findParTypeMethode(String fse, TypeMethode typeMethode);
    @Query("select i from InterfaceUser i where i.fournisseurService.codeFse=?1 and i.typeMethode=?2  and i.typeInterface=?3")
    public InterfaceUser findParTypeMethode(String fse, TypeMethode typeMethode, TypeEnvoi typeEnv);
    
}
