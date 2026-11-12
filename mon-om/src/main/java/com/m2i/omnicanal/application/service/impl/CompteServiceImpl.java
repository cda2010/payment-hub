package com.m2i.omnicanal.application.service.impl;


import com.m2i.omnicanal.application.domain.AgenceFse;
import com.m2i.omnicanal.application.domain.CompteFse;
import com.m2i.omnicanal.application.domain.ServiceMethod;
import com.m2i.omnicanal.application.repository.CompteRepository;
import com.m2i.omnicanal.application.service.CompteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Implementation for managing Compte.
 */
@Service
@Transactional
public class CompteServiceImpl implements CompteService{

    private final Logger log = LoggerFactory.getLogger(CompteServiceImpl.class);
    
    private final CompteRepository compteRepository;

    public CompteServiceImpl(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    /**
     * Save a compte.
     *
     * @param compte the entity to save
     * @return the persisted entity
     */
    @Override
    public CompteFse save(CompteFse compte) {
   
                 
        log.debug("Request to save Compte : {}", compte);
        CompteFse result = compteRepository.save(compte);
        return result;
    }

    /**
     *  Get all the comptes.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<CompteFse> findAll() {
        log.debug("Request to get all Comptes");
        List<CompteFse> result = compteRepository.findAll();

        return result;
    }

    /**
     *  Get one compte by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public CompteFse findOne(Long id) {
        log.debug("Request to get Compte : {}", id);
        CompteFse compte = compteRepository.findOne(id);
        return compte;
    }

    /**
     *  Delete the  compte by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Compte : {}", id);
        compteRepository.deleteById(id);
    }

    @Override
    public List<CompteFse> findByRelation(Long idrelation) {
       List<CompteFse> result =compteRepository.findByRelation(idrelation);
       return result;
    }

    @Override
    public CompteFse findCompteFse(String codeFseTitulaire, String codeParametre, String codeUoTitulaire, Long codeTypeService) {
        CompteFse compte = compteRepository.findCompteFse( codeFseTitulaire,  codeParametre,  codeUoTitulaire,  codeTypeService);
        return compte;
    }

    @Override
    public List<CompteFse> findCompteMorroir(Long idCompte) {
          List<CompteFse> result;
      if (idCompte==0){
           result = compteRepository.findAll();

      }else{
          result = compteRepository.findCompteMorroir(idCompte);
      }
      return result;    
    }

    @Override
    public Page<CompteFse> findAll(Pageable pageable, Long teneur, Long titulaire, Long type) {
         log.debug("Request to get all Agences");
        Page<CompteFse> result = compteRepository.findAll(pageable,  teneur,  titulaire,  type);
        return result;
    }
}
