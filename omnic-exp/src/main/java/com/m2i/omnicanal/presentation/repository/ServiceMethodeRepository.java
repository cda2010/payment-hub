/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.repository;


import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.model.ServiceTraitement;
import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.model.enumeration.TypeMethode;
import com.m2i.omnicanal.presentation.domain.FournisseurFSE;
import com.m2i.omnicanal.presentation.domain.Service;

import com.m2i.omnicanal.presentation.domain.ServiceMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author cda
 */
public interface ServiceMethodeRepository extends EntityRepository<ServiceMethod,Long>{
    @Query("select a from  ServiceMethod   a where a.methode.api.fournisseurService=?1 and a.service.typeService=?2  and a.methode.typeMethode=?3")
    ServiceMethod findMethodeByFournisseurService(FournisseurService fourn, TypeService serv,TypeMethode typeMethod );
}
