package com.m2i.pti.service;

import com.m2i.pti.domain.FileExportParam;
import com.m2i.pti.repository.FileExportParamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing fileExportParam.
 */
@Service
@Transactional
public class FileExportParamService {

    private final Logger log = LoggerFactory.getLogger(FileExportParamService.class);

    private final FileExportParamRepository fileExportParamRepository;

    public FileExportParamService(FileExportParamRepository fileExportParamRepository) {
        this.fileExportParamRepository = fileExportParamRepository;
    }

    
    public FileExportParam save(FileExportParam fileExportParam) {
        log.debug("Request to save FileExportParam : {}", fileExportParam);
        return fileExportParamRepository.save(fileExportParam);
    }

    
    @Transactional(readOnly = true)
    public Page<FileExportParam> findAll(Pageable pageable) {
        log.debug("Request to get all FileExportParam");
        return fileExportParamRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public FileExportParam findOne(Long id) {
        log.debug("Request to get FileExportParam : {}", id);
        return fileExportParamRepository.findById(id).orElse(null);
    }

    
    public void delete(Long id) {
        log.debug("Request to delete FileExportParam : {}", id);
        fileExportParamRepository.deleteById(id);
    }
}
