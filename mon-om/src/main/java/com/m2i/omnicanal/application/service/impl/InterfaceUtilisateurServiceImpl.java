package com.m2i.omnicanal.application.service.impl;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m2i.omnicanal.application.domain.InterfaceUser;
import com.m2i.omnicanal.application.domain.ParametreUser;
import com.m2i.omnicanal.application.repository.InterfaceUtilisateurRepository;
import com.m2i.omnicanal.application.service.InterfaceUtilisateurService;
import com.m2i.omnicanal.application.service.ParametreUtiliateurService;

/**
 * Service Implementation for managing InterfaceUtilisateur.
 */
@Service
@Transactional
public class InterfaceUtilisateurServiceImpl implements InterfaceUtilisateurService{

    private final Logger log = LoggerFactory.getLogger(InterfaceUtilisateurServiceImpl.class);
    
    private final InterfaceUtilisateurRepository interfaceUtilisateurRepository;

    @Autowired
    private ParametreUtiliateurService parametreUtiliateurService;

    public InterfaceUtilisateurServiceImpl(InterfaceUtilisateurRepository interfaceUtilisateurRepository) {
        this.interfaceUtilisateurRepository = interfaceUtilisateurRepository;
    }

    /**
     * Save a interfaceUtilisateur.
     *
     * @param interfaceUtilisateur the entity to save
     * @return the persisted entity
     */
    @Override
    public InterfaceUser save(InterfaceUser interfaceUtilisateur) {
        log.debug("Request to save InterfaceUtilisateur : {}", interfaceUtilisateur);
        InterfaceUser result = interfaceUtilisateurRepository.save(interfaceUtilisateur);
        return result;
    }

    /**
     *  Get all the interfaceUtilisateurs.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<InterfaceUser> findAll() {
        log.debug("Request to get all InterfaceUtilisateurs");
        List<InterfaceUser> result = interfaceUtilisateurRepository.findAll();

        return result;
    }

    /**
     *  Get one interfaceUtilisateur by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public InterfaceUser findOne(Long id) {
        log.debug("Request to get InterfaceUtilisateur : {}", id);
        InterfaceUser interfaceUtilisateur = interfaceUtilisateurRepository.findOne(id);
        return interfaceUtilisateur;
    }

    /**
     *  Delete the  interfaceUtilisateur by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete InterfaceUtilisateur : {}", id);
        interfaceUtilisateurRepository.deleteById(id);
    }

    @Override
    public InterfaceUser duplicate(InterfaceUser interfaceUtilisateur, InterfaceUser newInterfaceUtilisateur) {
        //enregistrer une nouvelle interface
        InterfaceUser newInterface = save(newInterfaceUtilisateur);
        
        //recuperer les parametres de l'interface originale
        List<ParametreUser> originalParams = parametreUtiliateurService.findByInterface(interfaceUtilisateur.getId());
        log.debug("originalParams : {}", originalParams);
        //dupliquer chaque parametre
        for (ParametreUser param : originalParams) {
            ParametreUser newParam = new ParametreUser();  
            newParam.setCodeParametre(param.getCodeParametre());
            newParam.setNomParametre(param.getNomParametre());
            newParam.setValeur(param.getValeur());
            newParam.setTypeParametre(param.getTypeParametre());
            newParam.setParametreApi(param.getParametreApi());
            newParam.setAttribut(param.getAttribut());
            newParam.setCodeParametreMethode(param.getCodeParametreMethode());
            newParam.setInterfaceUtilisateur(newInterface);
            //enregistrer le nouveau parametre
            parametreUtiliateurService.save(newParam);
        }
        
        return newInterface;
    }
}
