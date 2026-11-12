package com.m2i.pti.service;

import com.m2i.pti.domain.Ftp;
import com.m2i.pti.repository.FtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing Ftp.
 */
@Service
@Transactional
public class FtpService {

    private final Logger log = LoggerFactory.getLogger(FtpService.class);

    private final FtpRepository ftpRepository;

    public FtpService(FtpRepository ftpRepository) {
        this.ftpRepository = ftpRepository;
    }

    /**
     * Save a ftp.
     *
     * @param ftp the entity to save
     * @return the persisted entity
     */
    public Ftp save(Ftp ftp) {
        log.debug("Request to save Ftp : {}", ftp);
        return ftpRepository.save(ftp);
    }

    /**
     * Get all the ftps.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Ftp> findAll(Pageable pageable) {
        log.debug("Request to get all Ftps");
        return ftpRepository.findAll(pageable);
    }

    /**
     * Get one ftp by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Ftp findOne(Long id) {
        log.debug("Request to get Ftp : {}", id);
        return ftpRepository.findById(id).orElse(null);
    }

    /**
     * Delete the ftp by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Ftp : {}", id);
        ftpRepository.deleteById(id);
    }
}
