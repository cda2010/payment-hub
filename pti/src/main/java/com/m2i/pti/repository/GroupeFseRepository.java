/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

import com.m2i.pti.domain.GroupeFse;
import com.m2i.pti.mobile.dto.MGroupeUniteOrganisation;
import com.m2i.pti.service.dto.CompteDTO;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author cda
 */
@Repository
public interface GroupeFseRepository extends JpaRepository<GroupeFse, Long> {

      @Query(name = "groupeFse.findAllGroupe", nativeQuery = true)
        List<MGroupeUniteOrganisation> findAllGroupeFse();

}
