package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.application.domain.InterfaceUser;

import com.m2i.omnicanal.model.Operation;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;


/**
 * Spring Data JPA repository for the InterfaceUtilisateur entity.
 */
@SuppressWarnings("unused")
public interface InterfaceUtilisateurRepository extends EntityRepository<InterfaceUser,Long> {

    @Query("select i from InterfaceUser i where ((i.fournisseurService.codeFse = coalesce(:pfse,i.fournisseurService.codeFse)) AND" +
        " (i.typeMethode = coalesce(:ptypeMethode,i.typeMethode))) order by i.codeInterface desc" )
    List<InterfaceUser> rechercheIU(@Param("ptypeMethode") String ptypeMethode,
                                            @Param("pfse") String pfse);

}
