package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.BackupData;
import com.m2i.pti.repository.BackupDataRepository;
import com.m2i.pti.service.BackupDataService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.BackupDataDTO;
import com.m2i.pti.service.mapper.BackupDataMapper;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Notification.
 */
@RestController
@RequestMapping("/api")
public class BackupDataResource {

    private final Logger log = LoggerFactory.getLogger(BackupDataResource.class);

    private static final String ENTITY_NAME = "backupData";

    private final BackupDataService backupDataService;

    private final BackupDataRepository backupDataRepository;

    private final BackupDataMapper backupDataMapper;

    public BackupDataResource(BackupDataService backupDataService,BackupDataRepository backupDataRepository,BackupDataMapper backupDataMapper) {
        this.backupDataService = backupDataService;
        this.backupDataRepository = backupDataRepository;
        this.backupDataMapper = backupDataMapper;
    }

    /**
     * POST  /etapeService : Create a new notification.
     *
     * @param backupServiceDTO the etapeServiceDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new etapeServiceDTO, or with status 400 (Bad Request) if the notification has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/backup-data")
    @Timed
    public ResponseEntity<BackupDataDTO> createBackupData(@RequestBody BackupDataDTO backupDataDTO) throws URISyntaxException {  
        log.debug("REST request to save EtapeServiceService : {}", backupDataDTO);
        if (backupDataDTO.getId() != null) {
            throw new BadRequestAlertException("A new etape service cannot already have an ID", ENTITY_NAME, "idexists");
        }  
        BackupDataDTO result = backupDataService.save(backupDataDTO);
        return ResponseEntity.created(new URI("/api/backup-data/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /etapeService : Updates an existing notification.
     * 
     * @param etapeServiceDTO the etapeServiceDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated etapeServiceDTO,
     * or with status 400 (Bad Request) if the etapeServiceDTO is not valid,
     * or with status 500 (Internal Server Error) if the etapeServiceDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/backup-data")
    @Timed
    public ResponseEntity<BackupDataDTO> updateBackupData(@RequestBody BackupDataDTO backupDataDTO) throws URISyntaxException {
        log.debug("REST request to update Notification : {}", backupDataDTO);
        if (backupDataDTO.getId() == null) {
            return createBackupData(backupDataDTO);
        }
        BackupDataDTO result = backupDataService.save(backupDataDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, backupDataDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /etapeService : get all the etapeService.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of etapeService in body
     */
    @GetMapping("/backup-data")
    @Timed
    public ResponseEntity<List<BackupDataDTO>> getAllBackupData(@RequestParam(required = false) ZonedDateTime dateUpdate, Pageable pageable) {
        Page<BackupDataDTO> page = backupDataService.findAllBackupByDate(dateUpdate,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/backup-data");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK); 
    }

    /**
     * GET  /etapeService/:id : get the "id" notification.
     *
     * @param id the id of the etapeServiceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the etapeServiceDTO, or with status 404 (Not Found)
     */
    @GetMapping("/backup-data/{id}")
    @Timed
    public ResponseEntity<BackupDataDTO> getBackupData(@PathVariable Long id) {
        BackupData backupData = backupDataRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(backupDataMapper.toDto(backupData)));
    }

    /**
     * DELETE  /etapeService/:id : delete the "id" notification.
     *
     * @param id the id of the etapeServiceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/backup-data/{id}")
    @Timed
    public ResponseEntity<Void> deleteBackupData(@PathVariable Long id) {
        log.debug("REST request to delete BackupData : {}", id);
        backupDataRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
