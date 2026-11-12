/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

import com.m2i.pti.domain.UniteOrganisationCorrespondance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author cda
 */
@Repository
public interface UniteOrganisationCorrespondanceRepository extends JpaRepository<UniteOrganisationCorrespondance, Long>{
    
    @Query("select u from UniteOrganisationCorrespondance u where u.codeFseTeneur=?1 ")
    UniteOrganisationCorrespondance findByCodeTeneur(String codeTeneur);
}
