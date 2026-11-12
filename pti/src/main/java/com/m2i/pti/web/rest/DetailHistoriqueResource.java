package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.DetailHistoriqueService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.DetailHistoriqueDTO;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing DetailHistorique.
 */
@RestController
@RequestMapping("/api")
public class DetailHistoriqueResource {

    private final Logger log = LoggerFactory.getLogger(DetailHistoriqueResource.class);

    private static final String ENTITY_NAME = "detailHistorique";

    private final DetailHistoriqueService detailHistoriqueService;

    public DetailHistoriqueResource(DetailHistoriqueService detailHistoriqueService) {
        this.detailHistoriqueService = detailHistoriqueService;
    }

    /**
     * POST  /detail-historiques : Create a new detailHistorique.
     *
     * @param detailHistoriqueDTO the detailHistoriqueDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new detailHistoriqueDTO, or with status 400 (Bad Request) if the detailHistorique has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/detail-historiques")
    @Timed
    public ResponseEntity<DetailHistoriqueDTO> createDetailHistorique(@RequestBody DetailHistoriqueDTO detailHistoriqueDTO) throws URISyntaxException {
        log.debug("REST request to save DetailHistorique : {}", detailHistoriqueDTO);
        if (detailHistoriqueDTO.getId() != null) {
            throw new BadRequestAlertException("A new detailHistorique cannot already have an ID", ENTITY_NAME, "idexists");
        }
        DetailHistoriqueDTO result = detailHistoriqueService.save(detailHistoriqueDTO);
        return ResponseEntity.created(new URI("/api/detail-historiques/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /detail-historiques : Updates an existing detailHistorique.
     *
     * @param detailHistoriqueDTO the detailHistoriqueDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated detailHistoriqueDTO,
     * or with status 400 (Bad Request) if the detailHistoriqueDTO is not valid,
     * or with status 500 (Internal Server Error) if the detailHistoriqueDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/detail-historiques")
    @Timed
    public ResponseEntity<DetailHistoriqueDTO> updateDetailHistorique(@RequestBody DetailHistoriqueDTO detailHistoriqueDTO) throws URISyntaxException {
        log.debug("REST request to update DetailHistorique : {}", detailHistoriqueDTO);
        if (detailHistoriqueDTO.getId() == null) {
            return createDetailHistorique(detailHistoriqueDTO);
        }
        DetailHistoriqueDTO result = detailHistoriqueService.save(detailHistoriqueDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, detailHistoriqueDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /detail-historiques : get all the detailHistoriques.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of detailHistoriques in body
     */
    @GetMapping("/detail-historiques")
    @Timed
    public ResponseEntity<List<DetailHistoriqueDTO>> getAllDetailHistoriques(@ApiParam Pageable pageable, @RequestParam String typeHistorique, @RequestParam String dateEnregistrement,@RequestParam(required = false) String telephone) {
        log.debug("REST request to get a page of DetailHistoriques");
        Page<DetailHistoriqueDTO> page = detailHistoriqueService.findAll(pageable,typeHistorique,dateEnregistrement,telephone);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/detail-historiques");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /detail-historiques/:id : get the "id" detailHistorique.
     *
     * @param id the id of the detailHistoriqueDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the detailHistoriqueDTO, or with status 404 (Not Found)
     */
    @GetMapping("/detail-historiques/{id}")
    @Timed
    public ResponseEntity<DetailHistoriqueDTO> getDetailHistorique(@PathVariable Long id) {
        log.debug("REST request to get DetailHistorique : {}", id);
        DetailHistoriqueDTO detailHistoriqueDTO = detailHistoriqueService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(detailHistoriqueDTO));
    }

    /**
     * DELETE  /detail-historiques/:id : delete the "id" detailHistorique.
     *
     * @param id the id of the detailHistoriqueDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/detail-historiques/{id}")
    @Timed
    public ResponseEntity<Void> deleteDetailHistorique(@PathVariable Long id) {
        log.debug("REST request to delete DetailHistorique : {}", id);
        detailHistoriqueService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
