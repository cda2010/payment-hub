package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.TauxChange;
import com.m2i.pti.domain.enumeration.TypeChange;
import com.m2i.pti.service.TauxChangeService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.TauxChangeDTO;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing TauxChange.
 */
@RestController
@RequestMapping("/api")
public class TauxChangeResource {

    private final Logger log = LoggerFactory.getLogger(TauxChangeResource.class);

    private static final String ENTITY_NAME = "tauxChange";

    private final TauxChangeService tauxChangeService;

    public TauxChangeResource(TauxChangeService tauxChangeService) {
        this.tauxChangeService = tauxChangeService;
    }

    /**
     * POST  /taux-changes : Create a new tauxChange.
     *
     * @param tauxChangeDTO the tauxChangeDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new tauxChangeDTO, or with status 400 (Bad Request) if the tauxChange has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/taux-changes")
    @Timed
    public ResponseEntity<TauxChangeDTO> createTauxChange(@Valid @RequestBody TauxChangeDTO tauxChangeDTO) throws URISyntaxException {
        log.debug("REST request to save TauxChange : {}", tauxChangeDTO);
        if (tauxChangeDTO.getId() != null) {
            throw new BadRequestAlertException("A new tauxChange cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TauxChangeDTO result = tauxChangeService.save(tauxChangeDTO);
        return ResponseEntity.created(new URI("/api/taux-changes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /taux-changes : Updates an existing tauxChange.
     *
     * @param tauxChangeDTO the tauxChangeDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated tauxChangeDTO,
     * or with status 400 (Bad Request) if the tauxChangeDTO is not valid,
     * or with status 500 (Internal Server Error) if the tauxChangeDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/taux-changes")
    @Timed
    public ResponseEntity<TauxChangeDTO> updateTauxChange(@Valid @RequestBody TauxChangeDTO tauxChangeDTO) throws URISyntaxException {
        log.debug("REST request to update TauxChange : {}", tauxChangeDTO);
        if (tauxChangeDTO.getId() == null) {
            return createTauxChange(tauxChangeDTO);
        }
        TauxChangeDTO result = tauxChangeService.save(tauxChangeDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, tauxChangeDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /taux-changes : get all the tauxChanges.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of tauxChanges in body
     */
    @GetMapping("/taux-changes")
    @Timed
    public ResponseEntity<List<TauxChangeDTO>> getAllTauxChanges(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of TauxChanges");
        Page<TauxChangeDTO> page = tauxChangeService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/taux-changes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     *  Recherche en fonction de plusieurs critères
     * @param arriveeId
     * @param typeChange
     * @return
     */
    @GetMapping("/taux-changes/byCriteria")
    @Timed
    public TauxChangeDTO getAllTauxChanges(@RequestParam Long arriveeId, @RequestParam TypeChange typeChange,@RequestParam(required = false) Long departId) {
       return  tauxChangeService.findByCriteriaOfUserConnecte(departId,arriveeId,LocalDate.now(),typeChange);
    }



    /**
     * GET  /taux-changes/:id : get the "id" tauxChange.
     *
     * @param id the id of the tauxChangeDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the tauxChangeDTO, or with status 404 (Not Found)
     */
    @GetMapping("/taux-changes/{id}")
    @Timed
    public ResponseEntity<TauxChangeDTO> getTauxChange(@PathVariable Long id) {
        log.debug("REST request to get TauxChange : {}", id);
        TauxChangeDTO tauxChangeDTO = tauxChangeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(tauxChangeDTO));
    }

    /**
     * DELETE  /taux-changes/:id : delete the "id" tauxChange.
     *
     * @param id the id of the tauxChangeDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/taux-changes/{id}")
    @Timed
    public ResponseEntity<Void> deleteTauxChange(@PathVariable Long id) {
        log.debug("REST request to delete TauxChange : {}", id);
        tauxChangeService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
