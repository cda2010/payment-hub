/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.application.domain.WebServiceUser;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author cda
 */
public interface WebServiceRepository extends EntityRepository<WebServiceUser,Long> {
    
}
