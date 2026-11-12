package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.DetailListeDiffusionService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.DetailListeDiffusionDTO;
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

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing DetailListeDiffusion.
 */
@RestController
@RequestMapping("/api")
public class DetailListeDiffusionResource {

    private final Logger log = LoggerFactory.getLogger(DetailListeDiffusionResource.class);

    private static final String ENTITY_NAME = "detailListeDiffusion";

    private final DetailListeDiffusionService detailListeDiffusionService;

    public DetailListeDiffusionResource(DetailListeDiffusionService detailListeDiffusionService) {
        this.detailListeDiffusionService = detailListeDiffusionService;
    }

    /**
     * POST  /detail-liste-diffusions : Create a new detailListeDiffusion.
     *
     * @param detailListeDiffusionDTO the detailListeDiffusionDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new detailListeDiffusionDTO, or with status 400 (Bad Request) if the detailListeDiffusion has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/detail-liste-diffusions")
    @Timed
    public ResponseEntity<DetailListeDiffusionDTO> createDetailListeDiffusion(@RequestBody DetailListeDiffusionDTO detailListeDiffusionDTO) throws URISyntaxException {
        log.debug("REST request to save DetailListeDiffusion : {}", detailListeDiffusionDTO);
        if (detailListeDiffusionDTO.getId() != null) {
            throw new BadRequestAlertException("A new detailListeDiffusion cannot already have an ID", ENTITY_NAME, "idexists");
        }
        DetailListeDiffusionDTO result = detailListeDiffusionService.save(detailListeDiffusionDTO);
        return ResponseEntity.created(new URI("/api/detail-liste-diffusions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /detail-liste-diffusions : Updates an existing detailListeDiffusion.
     *
     * @param detailListeDiffusionDTO the detailListeDiffusionDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated detailListeDiffusionDTO,
     * or with status 400 (Bad Request) if the detailListeDiffusionDTO is not valid,
     * or with status 500 (Internal Server Error) if the detailListeDiffusionDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/detail-liste-diffusions")
    @Timed
    public ResponseEntity<DetailListeDiffusionDTO> updateDetailListeDiffusion(@RequestBody DetailListeDiffusionDTO detailListeDiffusionDTO) throws URISyntaxException {
        log.debug("REST request to update DetailListeDiffusion : {}", detailListeDiffusionDTO);
        if (detailListeDiffusionDTO.getId() == null) {
            return createDetailListeDiffusion(detailListeDiffusionDTO);
        }
        DetailListeDiffusionDTO result = detailListeDiffusionService.save(detailListeDiffusionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, detailListeDiffusionDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /detail-liste-diffusions : get all the detailListeDiffusions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of detailListeDiffusions in body
     */
    @GetMapping("/detail-liste-diffusions")
    @Timed
    public ResponseEntity<List<DetailListeDiffusionDTO>> getAllDetailListeDiffusions(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of DetailListeDiffusions");
        Page<DetailListeDiffusionDTO> page = detailListeDiffusionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/detail-liste-diffusions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /detail-liste-diffusions/:id : get the "id" detailListeDiffusion.
     *
     * @param id the id of the detailListeDiffusionDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the detailListeDiffusionDTO, or with status 404 (Not Found)
     */
    @GetMapping("/detail-liste-diffusions/{id}")
    @Timed
    public ResponseEntity<DetailListeDiffusionDTO> getDetailListeDiffusion(@PathVariable Long id) {
        log.debug("REST request to get DetailListeDiffusion : {}", id);
        DetailListeDiffusionDTO detailListeDiffusionDTO = detailListeDiffusionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(detailListeDiffusionDTO));
    }
    @GetMapping("/detail-liste-diffusions-of-liste-diffusion/{id}")
    @Timed
    public List<DetailListeDiffusionDTO> getDetailListeDiffusionOfListe(@PathVariable Long id) {
        log.debug("REST request to get DetailListeDiffusion : {}", id);
        return detailListeDiffusionService.findDetailOfList(id);
    }

    /**
     * DELETE  /detail-liste-diffusions/:id : delete the "id" detailListeDiffusion.
     *
     * @param id the id of the detailListeDiffusionDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/detail-liste-diffusions/{id}")
    @Timed
    public ResponseEntity<Void> deleteDetailListeDiffusion(@PathVariable Long id) {
        log.debug("REST request to delete DetailListeDiffusion : {}", id);
        detailListeDiffusionService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
