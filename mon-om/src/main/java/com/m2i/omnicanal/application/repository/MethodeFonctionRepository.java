/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.model.ServiceTraitement;
import com.m2i.omnicanal.model.enumeration.TypeMethode;
import com.m2i.omnicanal.application.domain.FournisseurFSE;
import com.m2i.omnicanal.application.domain.MethodeFonction;
import com.m2i.omnicanal.application.domain.Service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author cda
 */
public interface MethodeFonctionRepository extends EntityRepository<MethodeFonction,Long> {

   
}
