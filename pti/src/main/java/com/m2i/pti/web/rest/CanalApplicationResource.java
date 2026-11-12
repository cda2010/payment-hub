package com.m2i.pti.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.CanalApplicationService;
import com.m2i.pti.service.dto.CanalApplicationDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;

/**
 * REST controller for managing Canal.
 */
@RestController
@RequestMapping("/api")
public class CanalApplicationResource {

    private final Logger log = LoggerFactory.getLogger(CanalApplicationResource.class);

    private static final String ENTITY_NAME = "canal_application";

    private final CanalApplicationService canalApplicationService;

    public CanalApplicationResource(CanalApplicationService canalApplicationService) {
        this.canalApplicationService = canalApplicationService;
    }

    /**
     * POST  /canals : Create a new canal.
     *
     * @param canalDTO the canalDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new canalDTO, or with status 400 (Bad Request) if the canal has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/canal-application")
    @Timed
    public ResponseEntity<CanalApplicationDTO> createCanalApplication(@Valid @RequestBody CanalApplicationDTO canalApplicationDTO) throws URISyntaxException {
        log.debug("REST request to save Canal Application: {}", canalApplicationDTO);
        if (canalApplicationDTO.getId() != null) {
            throw new BadRequestAlertException("A new canal cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CanalApplicationDTO result = canalApplicationService.save(canalApplicationDTO);
        return ResponseEntity.created(new URI("/api/canal-application/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @GetMapping("/canal-application")
    @Timed
    public ResponseEntity<List<CanalApplicationDTO>> getAllCanalApplication(Pageable pageable) {
        Page<CanalApplicationDTO> page = canalApplicationService.getAllCanalApplication(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/canal-application");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK); 
    }

    @GetMapping("/canal-application-by-statut")
    @Timed
    public ResponseEntity<List<CanalApplicationDTO>> getAllCanalApplicationByCanal(@RequestParam(required = true) String typeCanal,Pageable pageable) {
        Page<CanalApplicationDTO> page = canalApplicationService.getAllCanalApplicationByCanal(typeCanal,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/canal-application-by-statut");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK); 
    }
/* 
    @GetMapping("/canal-application/{id}")
    @Timed
    public ResponseEntity<CanalApplicationDTO> getCanalApplication(@PathVariable Long id) {
        CanalApplicationDTO canalApplicationDTO = canalApplicationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(canalApplicationDTO));
    }
 */

    @DeleteMapping("/canal-application/{id}")
    @Timed
    public ResponseEntity<Void> deleteCanalApplication(@PathVariable Long id) {
        canalApplicationService.delete(id);
        return ResponseEntity
        .ok()
        .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
        .build();
    }

    @PutMapping("/canal-application")
    @Timed
    public ResponseEntity<CanalApplicationDTO> updateCanalParam(
        @Valid @RequestBody CanalApplicationDTO canalApplicationDTO
    )
        throws URISyntaxException {
        if (canalApplicationDTO.getId() == null) {
        return createCanalApplication(canalApplicationDTO);
        }
        CanalApplicationDTO result = canalApplicationService.save(canalApplicationDTO);
        return ResponseEntity
        .ok()
        .headers(
            HeaderUtil.createEntityUpdateAlert(
            ENTITY_NAME,
            canalApplicationDTO.getId().toString()
            )
        )
        .body(result);
    }

}
