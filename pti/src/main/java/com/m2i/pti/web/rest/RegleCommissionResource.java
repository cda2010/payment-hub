package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.RegleCommissionService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.RegleCommissionDTO;
import io.swagger.annotations.ApiParam;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing RegleCommission.
 */
@RestController
@RequestMapping("/api")
public class RegleCommissionResource {

    private final Logger log = LoggerFactory.getLogger(RegleCommissionResource.class);

    private static final String ENTITY_NAME = "regleCommission";

    private final RegleCommissionService regleCommissionService;

    public RegleCommissionResource(RegleCommissionService regleCommissionService) {
        this.regleCommissionService = regleCommissionService;
    }

    /**
     * POST  /regle-commissions : Create a new regleCommission.
     *
     * @param regleCommissionDTO the regleCommissionDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new regleCommissionDTO, or with status 400 (Bad Request) if the regleCommission has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/regle-commissions")
    @Timed
    public ResponseEntity<RegleCommissionDTO> createRegleCommission(@Valid @RequestBody RegleCommissionDTO regleCommissionDTO) throws URISyntaxException {
        log.debug("REST request to save RegleCommission : {}", regleCommissionDTO);
        if (regleCommissionDTO.getId() != null) {
            throw new BadRequestAlertException("A new regleCommission cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RegleCommissionDTO result = regleCommissionService.save(regleCommissionDTO);
        return ResponseEntity.created(new URI("/api/regle-commissions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /regle-commissions : Updates an existing regleCommission.
     *
     * @param regleCommissionDTO the regleCommissionDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated regleCommissionDTO,
     * or with status 400 (Bad Request) if the regleCommissionDTO is not valid,
     * or with status 500 (Internal Server Error) if the regleCommissionDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/regle-commissions")
    @Timed
    public ResponseEntity<RegleCommissionDTO> updateRegleCommission(@Valid @RequestBody RegleCommissionDTO regleCommissionDTO) throws URISyntaxException {
        log.debug("REST request to update RegleCommission : {}", regleCommissionDTO);
        if (regleCommissionDTO.getId() == null) {
            return createRegleCommission(regleCommissionDTO);
        }
        RegleCommissionDTO result = regleCommissionService.save(regleCommissionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, regleCommissionDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /regle-commissions : get all the regleCommissions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of regleCommissions in body
     */
    @GetMapping("/regle-commissions")
    @Timed
    public ResponseEntity<List<RegleCommissionDTO>> getAllRegleCommissions(@ApiParam Pageable pageable,@RequestParam(required = false) Long teneurSrcId,@RequestParam(required = false) Long teneurDestId,@RequestParam(required = false) Long typeServiceId) {
        log.debug("REST request to get a page of RegleCommissions");
        Page<RegleCommissionDTO> page = regleCommissionService.findAll(teneurSrcId,teneurDestId,typeServiceId,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/regle-commissions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /regle-commissions/:id : get the "id" regleCommission.
     *
     * @param id the id of the regleCommissionDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the regleCommissionDTO, or with status 404 (Not Found)
     */
    @GetMapping("/regle-commissions/{id}")
    @Timed
    public ResponseEntity<RegleCommissionDTO> getRegleCommission(@PathVariable Long id) {
        log.debug("REST request to get RegleCommission : {}", id);
        RegleCommissionDTO regleCommissionDTO = regleCommissionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(regleCommissionDTO));
    }

    /**
     * DELETE  /regle-commissions/:id : delete the "id" regleCommission.
     *
     * @param id the id of the regleCommissionDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/regle-commissions/{id}")
    @Timed
    public ResponseEntity<Void> deleteRegleCommission(@PathVariable Long id) {
        log.debug("REST request to delete RegleCommission : {}", id);
        regleCommissionService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
