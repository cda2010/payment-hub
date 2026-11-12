package com.m2i.pti.service;

import com.m2i.pti.domain.FileExport;
import com.m2i.pti.repository.FileExportRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing FileExport.
 */
@Service
@Transactional
public class FileExportService {

    private final Logger log = LoggerFactory.getLogger(FileExportService.class);

    private final FileExportRepository fileExportRepository;

    public FileExportService(FileExportRepository fileExportRepository) {
        this.fileExportRepository = fileExportRepository;
    }

    
    public FileExport save(FileExport fileExport) {
        log.debug("Request to save FileExport : {}", fileExport);
        return fileExportRepository.save(fileExport);
    }

    
    @Transactional(readOnly = true)
    public Page<FileExport> findAll(Pageable pageable) {
        log.debug("Request to get all FileExports");
        return fileExportRepository.findAll(pageable);
    }
    
    public List<FileExport> findAllList() {
        log.debug("Request to get all FileExports");
        return fileExportRepository.findAll();
    }

    @Transactional(readOnly = true)
    public FileExport findOne(Long id) {
        log.debug("Request to get FileExport : {}", id);
        return fileExportRepository.findById(id).orElse(null);
    }

    
    public void delete(Long id) {
        log.debug("Request to delete FileExport : {}", id);
        fileExportRepository.deleteById(id);
    }
}
