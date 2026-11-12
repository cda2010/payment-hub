/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

import com.m2i.pti.domain.UserListeBlanche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author cda
 */
@Repository
public interface  UserListeBlancheRepository extends JpaRepository<UserListeBlanche, Long>{
    @Query("select count(u) from UserListeBlanche u where login=?1")
    Long findCoundtByLogin(String login); 
}
