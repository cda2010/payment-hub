package com.m2i.pti.service;

import com.m2i.pti.domain.ParametreExport;
import com.m2i.pti.repository.PrametreExportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing PrametreExport.
 */
@Service
@Transactional
public class PrametreExportService {

    private final Logger log = LoggerFactory.getLogger(PrametreExportService.class);

    private final PrametreExportRepository prametreExportRepository;

    public PrametreExportService(PrametreExportRepository prametreExportRepository) {
        this.prametreExportRepository = prametreExportRepository;
    }

    /**
     * Save a prametreExport.
     *
     * @param parametreExport the entity to save
     * @return the persisted entity
     */
    public ParametreExport save(ParametreExport parametreExport) {
        log.debug("Request to save PrametreExport : {}", parametreExport);
        return prametreExportRepository.save(parametreExport);
    }

    /**
     * Get all the prametreExports.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ParametreExport> findAll(Pageable pageable) {
        log.debug("Request to get all PrametreExports");
        return prametreExportRepository.findAll(pageable);
    }

    /**
     * Get one prametreExport by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public ParametreExport findOne(Long id) {
        log.debug("Request to get PrametreExport : {}", id);
        return prametreExportRepository.findById(id).orElse(null);
    }

    /**
     * Delete the prametreExport by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete PrametreExport : {}", id);
        prametreExportRepository.deleteById(id);
    }
}
