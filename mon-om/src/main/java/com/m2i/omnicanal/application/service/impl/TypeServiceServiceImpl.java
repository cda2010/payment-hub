/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.service.impl;

import com.m2i.omnicanal.application.domain.TypeServ;
import com.m2i.omnicanal.application.repository.TypeServiceRepository;
import com.m2i.omnicanal.application.service.TypeServiceService;
import com.m2i.omnicanal.model.TypeService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author cda
 */
@Service
@Transactional
public class TypeServiceServiceImpl implements TypeServiceService {
 private final TypeServiceRepository typeServiceRepository;

    public TypeServiceServiceImpl(TypeServiceRepository typeServiceRepository) {
        this.typeServiceRepository = typeServiceRepository;
    }
 
    @Override
    public List<TypeService> findAll() {
       List<TypeService> result = typeServiceRepository.findAll();

        return result;
    }

    @Override
    public TypeService findOne(Long id) {
       TypeService result = typeServiceRepository.findOne(id);

        return result;
    }

    @Override 
    public TypeService save(TypeService serv) {
        TypeService result = typeServiceRepository.save(serv);
      return result;
    }
  
    @Override
    public void delete(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    
}
