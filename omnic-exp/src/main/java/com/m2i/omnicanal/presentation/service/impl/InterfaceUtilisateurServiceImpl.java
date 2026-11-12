package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.model.InterfaceUtilisateur;
import com.m2i.omnicanal.model.enumeration.TypeEnvoi;
import com.m2i.omnicanal.model.enumeration.TypeMethode;
import com.m2i.omnicanal.presentation.domain.InterfaceUser;
import com.m2i.omnicanal.presentation.repository.InterfaceUtilisateurRepository;
import com.m2i.omnicanal.presentation.service.InterfaceUtilisateurService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;

/**
 * Service Implementation for managing InterfaceUtilisateur.
 */
@Service
@Transactional
public class InterfaceUtilisateurServiceImpl implements InterfaceUtilisateurService {

    private final Logger log = LoggerFactory.getLogger(InterfaceUtilisateurServiceImpl.class);

    private final InterfaceUtilisateurRepository interfaceUtilisateurRepository;

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
     * Get all the interfaceUtilisateurs.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<InterfaceUser> findAll() {
        log.debug("Request to get all InterfaceUtilisateurs");
        List<InterfaceUser> result = interfaceUtilisateurRepository.findAll();

        return result;
    }

    /**
     * Get one interfaceUtilisateur by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public InterfaceUser findOne(Long id) {
        log.debug("Request to get InterfaceUtilisateur : {}", id);
        InterfaceUser interfaceUtilisateur = interfaceUtilisateurRepository.findOne(id);
        return interfaceUtilisateur;
    }

    /**
     * Delete the interfaceUtilisateur by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete InterfaceUtilisateur : {}", id);
        interfaceUtilisateurRepository.deleteById(id);
    }

    @Override
    public InterfaceUser findByService(String codeService) {
        InterfaceUser interfaceUtilisateur = interfaceUtilisateurRepository.findByInterService(codeService);
        return interfaceUtilisateur;
    }

    @Override
    public List<InterfaceUser> findByServiceFse(String fse, Long typeService) {
        List<InterfaceUser> interfaceUtilisateur = interfaceUtilisateurRepository.findByServiceFse(fse, typeService);
        return interfaceUtilisateur;
    }

   @Override
    @Cacheable(cacheNames = "interfaceUser",
               key = "T(java.lang.String).format('%s|%s|%s', #codeFse, #typeMethode.name(), #typeEnvoi.name())",
               unless = "#result == null")
    public InterfaceUser getInterfaceUser(String codeFse, TypeMethode typeMethode, TypeEnvoi typeEnvoi) {
      
        InterfaceUser interfaceUser;
        interfaceUser = interfaceUtilisateurRepository.findParTypeMethode(codeFse, typeMethode, typeEnvoi.REQUEST);
        return interfaceUser;
    }
}
