package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.FileExportParam;
import com.m2i.pti.service.FileExportParamService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import com.m2i.pti.repository.FileExportParamRepository;

/**
 * REST controller for managing FileExportParam.
 */
@RestController
@RequestMapping("/api")
public class FileExportParamResource {

    private final Logger log = LoggerFactory.getLogger(FileExportParamResource.class);

    private static final String ENTITY_NAME = "fileExportparam";

    private final FileExportParamService fileExportParamService;
    private final FileExportParamRepository fileExportParamRepository;

    public FileExportParamResource(FileExportParamService fileExportParamService, FileExportParamRepository fileExportParamRepository) {
        this.fileExportParamService = fileExportParamService;
        this.fileExportParamRepository = fileExportParamRepository;
    }

    @PostMapping("/file-export-params")
    @Timed
    public ResponseEntity<FileExportParam> createFileExportParam(@RequestBody FileExportParam fileExportParam) throws URISyntaxException {
        log.debug("REST request to save FileExportParam : {}", fileExportParam);
        log.debug("================================================{}", fileExportParam);
        if (fileExportParam.getId() != null) {
            throw new BadRequestAlertException("A new FileExportParam cannot already have an ID", ENTITY_NAME, "idexists");
        }
        FileExportParam result = fileExportParamService.save(fileExportParam);
        return ResponseEntity.created(new URI("/api/file-export-param/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @GetMapping("/file-export-params")
    @Timed
    public ResponseEntity<List<FileExportParam>> getAllFileExportsParam(Pageable pageable) {
        log.debug("REST request to get a page of FileExportParam");
        Page<FileExportParam> page = fileExportParamService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/file-export-params");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/file-export-params/{id}")
    @Timed
    public ResponseEntity<FileExportParam> getFileExportParam(@PathVariable Long id) {
        log.debug("REST request to get FileExportParam : {}", id);
        FileExportParam fileExportParam = fileExportParamService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(fileExportParam));
    }

    @DeleteMapping("/file-export-params/{id}")
    @Timed
    public ResponseEntity<Void> deleteFileExportParam(@PathVariable Long id) {
        log.debug("REST request to delete FileExportParam : {}", id);
        fileExportParamService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @PutMapping("/file-export-params")
    @Timed
    public ResponseEntity<FileExportParam> updateFileExport(@RequestBody FileExportParam fileExportParam) throws URISyntaxException {
        log.debug("------------------------------------------------------------------REST request to update file-export-params : {}", fileExportParam);
        if (fileExportParam.getId() == null) {
            return createFileExportParam(fileExportParam);
        }
        FileExportParam result = fileExportParamService.save(fileExportParam);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, fileExportParam.getId().toString()))
            .body(result);
    }

    @GetMapping("/file-export-params/of-file-export/{id}")
    @Timed
    public ResponseEntity<List<FileExportParam>> getAllFileExportParamByFileExportId(@PathVariable Long id, Pageable pageable) {
        Page<FileExportParam> page = fileExportParamRepository.findFileExportparamByFileExportId(id, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/file-export-params");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
