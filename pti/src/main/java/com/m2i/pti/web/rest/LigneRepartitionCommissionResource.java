package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.LigneRepartitionCommissionService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.LigneRepartitionCommissionDTO;
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
 * REST controller for managing LigneRepartitionCommission.
 */
@RestController
@RequestMapping("/api")
public class LigneRepartitionCommissionResource {

    private final Logger log = LoggerFactory.getLogger(LigneRepartitionCommissionResource.class);

    private static final String ENTITY_NAME = "ligneRepartitionCommission";

    private final LigneRepartitionCommissionService ligneRepartitionCommissionService;

    public LigneRepartitionCommissionResource(LigneRepartitionCommissionService ligneRepartitionCommissionService) {
        this.ligneRepartitionCommissionService = ligneRepartitionCommissionService;
    }

    /**
     * POST  /ligne-repartition-commissions : Create a new ligneRepartitionCommission.
     *
     * @param ligneRepartitionCommissionDTO the ligneRepartitionCommissionDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ligneRepartitionCommissionDTO, or with status 400 (Bad Request) if the ligneRepartitionCommission has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/ligne-repartition-commissions")
    @Timed
    public ResponseEntity<LigneRepartitionCommissionDTO> createLigneRepartitionCommission(@RequestBody LigneRepartitionCommissionDTO ligneRepartitionCommissionDTO) throws URISyntaxException {
        log.debug("REST request to save LigneRepartitionCommission : {}", ligneRepartitionCommissionDTO);
        if (ligneRepartitionCommissionDTO.getId() != null) {
            throw new BadRequestAlertException("A new ligneRepartitionCommission cannot already have an ID", ENTITY_NAME, "idexists");
        }
        LigneRepartitionCommissionDTO result = ligneRepartitionCommissionService.save(ligneRepartitionCommissionDTO);
        return ResponseEntity.created(new URI("/api/ligne-repartition-commissions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /ligne-repartition-commissions : Updates an existing ligneRepartitionCommission.
     *
     * @param ligneRepartitionCommissionDTO the ligneRepartitionCommissionDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ligneRepartitionCommissionDTO,
     * or with status 400 (Bad Request) if the ligneRepartitionCommissionDTO is not valid,
     * or with status 500 (Internal Server Error) if the ligneRepartitionCommissionDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/ligne-repartition-commissions")
    @Timed
    public ResponseEntity<LigneRepartitionCommissionDTO> updateLigneRepartitionCommission(@RequestBody LigneRepartitionCommissionDTO ligneRepartitionCommissionDTO) throws URISyntaxException {
        log.debug("REST request to update LigneRepartitionCommission : {}", ligneRepartitionCommissionDTO);
        if (ligneRepartitionCommissionDTO.getId() == null) {
            return createLigneRepartitionCommission(ligneRepartitionCommissionDTO);
        }
        LigneRepartitionCommissionDTO result = ligneRepartitionCommissionService.save(ligneRepartitionCommissionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ligneRepartitionCommissionDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /ligne-repartition-commissions : get all the ligneRepartitionCommissions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of ligneRepartitionCommissions in body
     */
    @GetMapping("/ligne-repartition-commissions")
    @Timed
    public ResponseEntity<List<LigneRepartitionCommissionDTO>> getAllLigneRepartitionCommissions(Pageable pageable) {
        log.debug("REST request to get a page of LigneRepartitionCommissions");
        Page<LigneRepartitionCommissionDTO> page = ligneRepartitionCommissionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/ligne-repartition-commissions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /ligne-repartition-commissions : get all the ligneRepartitionCommissions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of ligneRepartitionCommissions in body
     */
    @GetMapping("/ligne-repartition-commissions/{id}/tableauRepartition")
    @Timed
    public ResponseEntity<List<LigneRepartitionCommissionDTO>> getAllLigneRepartitionCommissionsByTableau(@PathVariable Long id) {
        log.debug("REST request to get a page of LigneRepartitionCommissions");
        //Page<LigneRepartitionCommissionDTO> page = ligneRepartitionCommissionService.findAllByTableauRepartition(id,pageable);
        List<LigneRepartitionCommissionDTO> page = ligneRepartitionCommissionService.findAllByTableauRepartitionList(id);
        //HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/ligne-repartition-commissions");
        return new ResponseEntity<>(page,HttpStatus.OK);
    }
/*    @GetMapping("/ligne-repartition-commissions/tableauRepartition/byId")
    @Timed
    public ResponseEntity<List<LigneRepartitionCommissionDTO>> getAllLigneRepartitionCommissionsByTableauById(@RequestParam Long id,Pageable pageable) {
        log.debug("REST request to get a page of LigneRepartitionCommissions");
        Page<LigneRepartitionCommissionDTO> page = ligneRepartitionCommissionService.findAllByTableauRepartitionNew(id,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/ligne-repartition-commissions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }*/

    @GetMapping("/ligne-repartition-commissions/tableauRepartition/byId")
    @Timed
    public List<LigneRepartitionCommissionDTO> getAllLigneRepartitionCommissionsByTableauById(@RequestParam Long id,Pageable pageable) {
        log.debug("REST request to get a page of LigneRepartitionCommissions");
        List<LigneRepartitionCommissionDTO> page = ligneRepartitionCommissionService.findAllByTableauRepartitionNewList(id,pageable);
        return page;
    }

    /**
     * GET  /ligne-repartition-commissions/:id : get the "id" ligneRepartitionCommission.
     *
     * @param id the id of the ligneRepartitionCommissionDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ligneRepartitionCommissionDTO, or with status 404 (Not Found)
     */
    @GetMapping("/ligne-repartition-commissions/{id}")
    @Timed
    public ResponseEntity<LigneRepartitionCommissionDTO> getLigneRepartitionCommission(@PathVariable Long id) {
        log.debug("REST request to get LigneRepartitionCommission : {}", id);
        LigneRepartitionCommissionDTO ligneRepartitionCommissionDTO = ligneRepartitionCommissionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ligneRepartitionCommissionDTO));
    }

    /**
     * DELETE  /ligne-repartition-commissions/:id : delete the "id" ligneRepartitionCommission.
     *
     * @param id the id of the ligneRepartitionCommissionDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/ligne-repartition-commissions/{id}")
    @Timed
    public ResponseEntity<Void> deleteLigneRepartitionCommission(@PathVariable Long id) {
        log.debug("REST request to delete LigneRepartitionCommission : {}", id);
        ligneRepartitionCommissionService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/ligne-repartition-commissions/tableauRepartition/byId-optimise")
    @Timed
    public List<LigneRepartitionCommissionDTO> getAllLigneRepartitionCommissionsByTableauByIdOptimise(@RequestParam Long id,Pageable pageable) {
        log.debug("REST request to get a page of LigneRepartitionCommissions");
        List<LigneRepartitionCommissionDTO> page = ligneRepartitionCommissionService.findAllByTableauRepartitionNewListOptimise(id,pageable);
        return page;
    }

}
