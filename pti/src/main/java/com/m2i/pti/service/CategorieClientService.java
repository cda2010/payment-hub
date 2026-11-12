package com.m2i.pti.service;

import com.m2i.pti.domain.CategorieClient;
import com.m2i.pti.repository.CategorieClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing CategorieClient.
 */
@Service
@Transactional
public class CategorieClientService {

    private final Logger log = LoggerFactory.getLogger(CategorieClientService.class);

    private final CategorieClientRepository categorieClientRepository;

    public CategorieClientService(CategorieClientRepository categorieClientRepository) {
        this.categorieClientRepository = categorieClientRepository;
    }

    /**
     * Save a categorieClient.
     *
     * @param categorieClient the entity to save
     * @return the persisted entity
     */
    public CategorieClient save(CategorieClient categorieClient) {
        log.debug("Request to save CategorieClient : {}", categorieClient);
        return categorieClientRepository.save(categorieClient);
    }

    /**
     *  Get all the categorieClients.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<CategorieClient> findAll(Pageable pageable) {
        log.debug("Request to get all CategorieClients");
        return categorieClientRepository.findAll(pageable);
    }

    /**
     *  Get one categorieClient by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public CategorieClient findOne(Long id) {
        log.debug("Request to get CategorieClient : {}", id);
        return categorieClientRepository.findById(id).orElse(null);
    }

    /**
     *  Delete the  categorieClient by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete CategorieClient : {}", id);
        categorieClientRepository.deleteById(id);
    }
}
