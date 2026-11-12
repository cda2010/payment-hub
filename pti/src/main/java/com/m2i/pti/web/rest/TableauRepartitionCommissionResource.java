package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.TableauRepartitionCommissionService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.TableauRepartitionCommissionDTO;
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
 * REST controller for managing TableauRepartitionCommission.
 */
@RestController
@RequestMapping("/api")
public class TableauRepartitionCommissionResource {

    private final Logger log = LoggerFactory.getLogger(TableauRepartitionCommissionResource.class);

    private static final String ENTITY_NAME = "tableauRepartitionCommission";

    private final TableauRepartitionCommissionService tableauRepartitionCommissionService;

    public TableauRepartitionCommissionResource(TableauRepartitionCommissionService tableauRepartitionCommissionService) {
        this.tableauRepartitionCommissionService = tableauRepartitionCommissionService;
    }

    /**
     * POST  /tableau-repartition-commissions : Create a new tableauRepartitionCommission.
     *
     * @param tableauRepartitionCommissionDTO the tableauRepartitionCommissionDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new tableauRepartitionCommissionDTO, or with status 400 (Bad Request) if the tableauRepartitionCommission has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/tableau-repartition-commissions")
    @Timed
    public ResponseEntity<TableauRepartitionCommissionDTO> createTableauRepartitionCommission(@RequestBody TableauRepartitionCommissionDTO tableauRepartitionCommissionDTO) throws URISyntaxException {
        log.debug("REST request to save TableauRepartitionCommission : {}", tableauRepartitionCommissionDTO);
        if (tableauRepartitionCommissionDTO.getId() != null) {
            throw new BadRequestAlertException("A new tableauRepartitionCommission cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TableauRepartitionCommissionDTO result = tableauRepartitionCommissionService.save(tableauRepartitionCommissionDTO);
        return ResponseEntity.created(new URI("/api/tableau-repartition-commissions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /tableau-repartition-commissions : Updates an existing tableauRepartitionCommission.
     *
     * @param tableauRepartitionCommissionDTO the tableauRepartitionCommissionDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated tableauRepartitionCommissionDTO,
     * or with status 400 (Bad Request) if the tableauRepartitionCommissionDTO is not valid,
     * or with status 500 (Internal Server Error) if the tableauRepartitionCommissionDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/tableau-repartition-commissions")
    @Timed
    public ResponseEntity<TableauRepartitionCommissionDTO> updateTableauRepartitionCommission(@RequestBody TableauRepartitionCommissionDTO tableauRepartitionCommissionDTO) throws URISyntaxException {
        log.debug("REST request to update TableauRepartitionCommission : {}", tableauRepartitionCommissionDTO);
        if (tableauRepartitionCommissionDTO.getId() == null) {
            return createTableauRepartitionCommission(tableauRepartitionCommissionDTO);
        }
        TableauRepartitionCommissionDTO result = tableauRepartitionCommissionService.save(tableauRepartitionCommissionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, tableauRepartitionCommissionDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /tableau-repartition-commissions : get all the tableauRepartitionCommissions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of tableauRepartitionCommissions in body
     */
    @GetMapping("/tableau-repartition-commissions")
    @Timed
    public ResponseEntity<List<TableauRepartitionCommissionDTO>> getAllTableauRepartitionCommissions(Pageable pageable) {
        log.debug("REST request to get a page of TableauRepartitionCommissions");
        Page<TableauRepartitionCommissionDTO> page = tableauRepartitionCommissionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/tableau-repartition-commissions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    @GetMapping("/tableau-repartition-commissions/group/{groupId}")
    @Timed
    public ResponseEntity<List<TableauRepartitionCommissionDTO>> getAllTableauRepartitionCommissionsByGroup(@PathVariable Long groupId, Pageable pageable) {
        log.debug("REST request to get a page of TableauRepartitionCommissions");
        Page<TableauRepartitionCommissionDTO> page = tableauRepartitionCommissionService.findAllByGroup(groupId,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/tableau-repartition-commissions/group/");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /tableau-repartition-commissions : get all the tableauRepartitionCommissions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of tableauRepartitionCommissions in body
     */
    @GetMapping("/tableau-repartition-commissions/search")
    @Timed
    public ResponseEntity<List<TableauRepartitionCommissionDTO>> getAllTableauFraisByMultiParm(@RequestParam(required = false) Long typeServiceId, @RequestParam(required = false) Integer niveau,Pageable pageable) {
        log.debug("REST request to get a page of TableauRepartitionCommissions");
        Page<TableauRepartitionCommissionDTO> page = tableauRepartitionCommissionService.findAllByMultiC(typeServiceId, niveau,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/tableau-repartition-commissions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /tableau-repartition-commissions/:id : get the "id" tableauRepartitionCommission.
     *
     * @param id the id of the tableauRepartitionCommissionDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the tableauRepartitionCommissionDTO, or with status 404 (Not Found)
     */
    @GetMapping("/tableau-repartition-commissions/{id}")
    @Timed
    public ResponseEntity<TableauRepartitionCommissionDTO> getTableauRepartitionCommission(@PathVariable Long id) {
        log.debug("REST request to get TableauRepartitionCommission : {}", id);
        TableauRepartitionCommissionDTO tableauRepartitionCommissionDTO = tableauRepartitionCommissionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(tableauRepartitionCommissionDTO));
    }

    /**
     * DELETE  /tableau-repartition-commissions/:id : delete the "id" tableauRepartitionCommission.
     *
     * @param id the id of the tableauRepartitionCommissionDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/tableau-repartition-commissions/{id}")
    @Timed
    public ResponseEntity<Void> deleteTableauRepartitionCommission(@PathVariable Long id) {
        log.debug("REST request to delete TableauRepartitionCommission : {}", id);
        tableauRepartitionCommissionService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/tableau-repartition-commissions/all")
    @Timed
    public List<TableauRepartitionCommissionDTO> findAll(){
        return tableauRepartitionCommissionService.findAll();
    }

    @GetMapping("/tableau-repartition-commissions-optimise/group/{groupId}")
    @Timed
    public ResponseEntity<List<TableauRepartitionCommissionDTO>> getAllTableauRepartitionCommissionsByGroupOptimise(@PathVariable Long groupId, Pageable pageable) {
        log.debug("REST request to get a page of TableauRepartitionCommissions");
        Page<TableauRepartitionCommissionDTO> page = tableauRepartitionCommissionService.findAllByGroupOptimise(groupId,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/tableau-repartition-commissions/group/");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
