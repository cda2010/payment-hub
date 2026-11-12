package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.MachineAuditEventService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.MachineAuditEventDTO;
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

/**
 * REST controller for managing MachineAuditEvent.
 */
@RestController
@RequestMapping("/api")
public class MachineAuditEventResource {

    private final Logger log = LoggerFactory.getLogger(MachineAuditEventResource.class);

    private static final String ENTITY_NAME = "machineAuditEvent";

    private final MachineAuditEventService machineAuditEventService;

    public MachineAuditEventResource(MachineAuditEventService machineAuditEventService) {
        this.machineAuditEventService = machineAuditEventService;
    }

    /**
     * POST  /machine-audit-events : Create a new machineAuditEvent.
     *
     * @param machineAuditEventDTO the machineAuditEventDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new machineAuditEventDTO, or with status 400 (Bad Request) if the machineAuditEvent has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/machine-audit-events")
    @Timed
    public ResponseEntity<MachineAuditEventDTO> createMachineAuditEvent(@RequestBody MachineAuditEventDTO machineAuditEventDTO) throws URISyntaxException {
        log.debug("REST request to save MachineAuditEvent : {}", machineAuditEventDTO);
        if (machineAuditEventDTO.getId() != null) {
            throw new BadRequestAlertException("A new machineAuditEvent cannot already have an ID", ENTITY_NAME, "idexists");
        }
        MachineAuditEventDTO result = machineAuditEventService.save(machineAuditEventDTO);
        return ResponseEntity.created(new URI("/api/machine-audit-events/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /machine-audit-events : Updates an existing machineAuditEvent.
     *
     * @param machineAuditEventDTO the machineAuditEventDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated machineAuditEventDTO,
     * or with status 400 (Bad Request) if the machineAuditEventDTO is not valid,
     * or with status 500 (Internal Server Error) if the machineAuditEventDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/machine-audit-events")
    @Timed
    public ResponseEntity<MachineAuditEventDTO> updateMachineAuditEvent(@RequestBody MachineAuditEventDTO machineAuditEventDTO) throws URISyntaxException {
        log.debug("REST request to update MachineAuditEvent : {}", machineAuditEventDTO);
        if (machineAuditEventDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        MachineAuditEventDTO result = machineAuditEventService.save(machineAuditEventDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, machineAuditEventDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /machine-audit-events : get all the machineAuditEvents.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of machineAuditEvents in body
     */
    @GetMapping("/machine-audit-events")
    @Timed
    public ResponseEntity<List<MachineAuditEventDTO>> getAllMachineAuditEvents(Pageable pageable, @RequestParam Long machineId) {
        log.debug("REST request to get a page of MachineAuditEvents");
        Page<MachineAuditEventDTO> page = machineAuditEventService.findAll(pageable,machineId);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/machine-audit-events");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/machine-audit-events/find_by_machine")
    @Timed
    public List<MachineAuditEventDTO> getAllMachineAuditEvents(@RequestParam Long machineId) {
        return machineAuditEventService.findByMachineId(machineId);
    }

    /**
     * GET  /machine-audit-events/:id : get the "id" machineAuditEvent.
     *
     * @param id the id of the machineAuditEventDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the machineAuditEventDTO, or with status 404 (Not Found)
     */
    @GetMapping("/machine-audit-events/{id}")
    @Timed
    public ResponseEntity<MachineAuditEventDTO> getMachineAuditEvent(@PathVariable Long id) {
        log.debug("REST request to get MachineAuditEvent : {}", id);
        MachineAuditEventDTO machineAuditEventDTO = machineAuditEventService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(machineAuditEventDTO));
    }

    /**
     * DELETE  /machine-audit-events/:id : delete the "id" machineAuditEvent.
     *
     * @param id the id of the machineAuditEventDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/machine-audit-events/{id}")
    @Timed
    public ResponseEntity<Void> deleteMachineAuditEvent(@PathVariable Long id) {
        log.debug("REST request to delete MachineAuditEvent : {}", id);
        machineAuditEventService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
