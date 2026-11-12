package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.domain.ParametreMethod;
import com.m2i.omnicanal.presentation.repository.ParametreMethodeRepository;
import com.m2i.omnicanal.presentation.service.ParametreMethodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import org.apache.commons.collections4.map.PassiveExpiringMap;

/**
 * Service Implementation for managing ParametreMethode.
 */
@Service
@Transactional
public class ParametreMethodeServiceImpl implements ParametreMethodeService {

    private final Logger log = LoggerFactory.getLogger(ParametreMethodeServiceImpl.class);

    private final ParametreMethodeRepository parametreMethodeRepository;
    PassiveExpiringMap<String, List<ParametreMethod>> parametreMethodMapDto = new PassiveExpiringMap<>(6400000);

    public ParametreMethodeServiceImpl(ParametreMethodeRepository parametreMethodeRepository) {
        this.parametreMethodeRepository = parametreMethodeRepository;
    }

    /**
     * Save a parametreMethode.
     *
     * @param parametreMethode the entity to save
     * @return the persisted entity
     */
    @Override
    public ParametreMethod save(ParametreMethod parametreMethode) {
        log.debug("Request to save ParametreMethode : {}", parametreMethode);
        ParametreMethod result = parametreMethodeRepository.save(parametreMethode);
        return result;
    }

    /**
     * Get all the parametreMethodes.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<ParametreMethod> findAll() {
        log.debug("Request to get all ParametreMethodes");
        List<ParametreMethod> result = parametreMethodeRepository.findAll();

        return result;
    }

    /**
     * Get one parametreMethode by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ParametreMethod findOne(Long id) {
        log.debug("Request to get ParametreMethode : {}", id);
        ParametreMethod parametreMethode = parametreMethodeRepository.findOne(id);
        return parametreMethode;
    }

    /**
     * Delete the parametreMethode by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ParametreMethode : {}", id);
        parametreMethodeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParametreMethod> findByMethode(Long idmethode) {
        log.debug("Request to get all ParametreMethodes by methode");
        List<ParametreMethod> result;
        if (parametreMethodMapDto.containsKey(idmethode.toString()) &&  parametreMethodMapDto.get(idmethode.toString())!=null) {
            result = parametreMethodMapDto.get(idmethode.toString());
        } else {
            result = parametreMethodeRepository.findByMethode(idmethode);
            if (result!=null){
                 parametreMethodMapDto.put(idmethode.toString(), result);
            }
           
        }
        return result;
    }
}
