package com.m2i.pti.service;

import com.m2i.pti.domain.ExportTable;
import com.m2i.pti.repository.ExportTableRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing ExportTable.
 */
@Service
@Transactional
public class ExportTableService {

    private final Logger log = LoggerFactory.getLogger(ExportTableService.class);

    private final ExportTableRepository exportTableRepository;

    public ExportTableService(ExportTableRepository exportTableRepository) {
        this.exportTableRepository = exportTableRepository;
    }

    /**
     * Save a exportTable.
     *
     * @param exportTable the entity to save
     * @return the persisted entity
     */
    public ExportTable save(ExportTable exportTable) {
        log.debug("Request to save ExportTable : {}", exportTable);
        return exportTableRepository.save(exportTable);
    }

    /**
     * Get all the exportTables.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ExportTable> findAll(Pageable pageable) {
        log.debug("Request to get all ExportTables");
        return exportTableRepository.findAll(pageable);
    }
    
     @Transactional(readOnly = true)
    public List<ExportTable> findAllList( ) {
        log.debug("Request to get all ExportTables");
        return exportTableRepository.findAll();
    }

    /**
     * Get one exportTable by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public ExportTable findOne(Long id) {
        log.debug("Request to get ExportTable : {}", id);
        return exportTableRepository.findById(id).orElse(null);
    }

    /**
     * Delete the exportTable by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete ExportTable : {}", id);
        exportTableRepository.deleteById(id);
    }
}
