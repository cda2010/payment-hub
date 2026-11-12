package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.repository.OperationAuditRepository;
import com.m2i.pti.service.OperationAuditService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.OperationAuditDTO;
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
 * REST controller for managing OperationAudit.
 */
@RestController
@RequestMapping("/api")
public class OperationAuditResource {

    private final Logger log = LoggerFactory.getLogger(OperationAuditResource.class);

    private static final String ENTITY_NAME = "operationAudit";

    private final OperationAuditService operationAuditService;
    private final OperationAuditRepository operationAuditRepository;

    public OperationAuditResource(OperationAuditService operationAuditService, OperationAuditRepository operationAuditRepository) {
        this.operationAuditService = operationAuditService;
        this.operationAuditRepository = operationAuditRepository;
    }

    /**
     * POST  /operation-audits : Create a new operationAudit.
     *
     * @param operationAuditDTO the operationAuditDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new operationAuditDTO, or with status 400 (Bad Request) if the operationAudit has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/operation-audits")
    @Timed
    public ResponseEntity<OperationAuditDTO> createOperationAudit(@RequestBody OperationAuditDTO operationAuditDTO) throws URISyntaxException {
        log.debug("REST request to save OperationAudit : {}", operationAuditDTO);
        if (operationAuditDTO.getId() != null) {
            throw new BadRequestAlertException("A new operationAudit cannot already have an ID", ENTITY_NAME, "idexists");
        }
        OperationAuditDTO result = operationAuditService.save(operationAuditDTO);
        return ResponseEntity.created(new URI("/api/operation-audits/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /operation-audits : Updates an existing operationAudit.
     *
     * @param operationAuditDTO the operationAuditDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated operationAuditDTO,
     * or with status 400 (Bad Request) if the operationAuditDTO is not valid,
     * or with status 500 (Internal Server Error) if the operationAuditDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/operation-audits")
    @Timed
    public ResponseEntity<OperationAuditDTO> updateOperationAudit(@RequestBody OperationAuditDTO operationAuditDTO) throws URISyntaxException {
        log.debug("REST request to update OperationAudit : {}", operationAuditDTO);
        if (operationAuditDTO.getId() == null) {
            return createOperationAudit(operationAuditDTO);
        }
        OperationAuditDTO result = operationAuditService.save(operationAuditDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, operationAuditDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /operation-audits : get all the operationAudits.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of operationAudits in body
     */
    @GetMapping("/operation-audits")
    @Timed
    public ResponseEntity<List<OperationAuditDTO>> getAllOperationAudits(@RequestParam(required = false) StatutOperation statutOperation, @RequestParam(required = false) StatutOperation statutApres, @RequestParam(required = false) Long userId, @RequestParam(required = false)ZonedDateTime debut, @RequestParam(required = false) ZonedDateTime fin, Pageable pageable) {
        log.debug("REST request to get a page of OperationAudits");
        Page<OperationAuditDTO> page = operationAuditService.findAll(statutOperation, statutApres,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/operation-audits");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /operation-audits/:id : get the "id" operationAudit.
     *
     * @param id the id of the operationAuditDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the operationAuditDTO, or with status 404 (Not Found)
     */
    @GetMapping("/operation-audits/{id}")
    @Timed
    public ResponseEntity<OperationAuditDTO> getOperationAudit(@PathVariable Long id) {
        log.debug("REST request to get OperationAudit : {}", id);
        OperationAuditDTO operationAuditDTO = operationAuditService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(operationAuditDTO));
    }

    /**
     * DELETE  /operation-audits/:id : delete the "id" operationAudit.
     *
     * @param id the id of the operationAuditDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/operation-audits/{id}")
    @Timed
    public ResponseEntity<Void> deleteOperationAudit(@PathVariable Long id) {
        log.debug("REST request to delete OperationAudit : {}", id);
        operationAuditService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
