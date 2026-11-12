/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.service;

import com.m2i.omnicanal.application.domain.TypeServ;
import com.m2i.omnicanal.model.TypeService;
import java.util.List;

/**
 *
 * @author cda
 */
public interface TypeServiceService {
     List<TypeService> findAll();

    public TypeService findOne(Long id);

    public TypeService save(TypeService serv);

    public void delete(Long id);
}
