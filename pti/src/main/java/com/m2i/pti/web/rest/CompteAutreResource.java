package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.CompteAutre;
import com.m2i.pti.service.CompteAutreService;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.dto.CompteDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.CompteAutreDTO;
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
 * REST controller for managing CompteAutre.
 */
@RestController
@RequestMapping("/api")
public class CompteAutreResource {

    private final Logger log = LoggerFactory.getLogger(CompteAutreResource.class);

    private static final String ENTITY_NAME = "compteAutre";

    private final CompteAutreService compteAutreService;
    private final CompteService compteService;

    public CompteAutreResource(CompteAutreService compteAutreService, CompteService compteService) {
        this.compteAutreService = compteAutreService;
        this.compteService = compteService;
    }

    /**
     * POST  /compte-autres : Create a new compteAutre.
     *
     * @param compteAutreDTO the compteAutreDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new compteAutreDTO, or with status 400 (Bad Request) if the compteAutre has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/compte-autres")
    @Timed
    public ResponseEntity<CompteAutreDTO> createCompteAutre(@RequestBody CompteAutreDTO compteAutreDTO) throws URISyntaxException {
        log.debug("REST request to save CompteAutre : {}", compteAutreDTO);
        if (compteAutreDTO.getId() != null) {
            throw new BadRequestAlertException("A new compteAutre cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CompteAutreDTO result = compteAutreService.save(compteAutreDTO);
        return ResponseEntity.created(new URI("/api/compte-autres/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /compte-autres : Updates an existing compteAutre.
     *
     * @param compteAutreDTO the compteAutreDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated compteAutreDTO,
     * or with status 400 (Bad Request) if the compteAutreDTO is not valid,
     * or with status 500 (Internal Server Error) if the compteAutreDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/compte-autres")
    @Timed
    public ResponseEntity<CompteAutreDTO> updateCompteAutre(@RequestBody CompteAutreDTO compteAutreDTO) throws URISyntaxException {
        log.debug("REST request to update CompteAutre : {}", compteAutreDTO);
        if (compteAutreDTO.getId() == null) {
            return createCompteAutre(compteAutreDTO);
        }
        CompteAutreDTO result = compteAutreService.save(compteAutreDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, compteAutreDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /compte-autres : get all the compteAutres.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of compteAutres in body
     */
    @GetMapping("/compte-autres")
    @Timed
    public ResponseEntity<List<CompteAutreDTO>> getAllCompteAutres(Pageable pageable) {
        log.debug("REST request to get a page of CompteAutres");
        Page<CompteAutreDTO> page = compteAutreService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/compte-autres");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/compte-autres/by-etiquette")
    @Timed
    public ResponseEntity<CompteAutreDTO> getCompteByetiquette(String etiquette) {
        log.debug("REST request to get Compte : {}", etiquette);
        CompteAutreDTO compteAutreDTO = compteService.getCompteAutreOfUserConnecte(etiquette);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(compteAutreDTO));
    }

    /**
     * GET  /compte-autres/:id : get the "id" compteAutre.
     *
     * @param id the id of the compteAutreDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the compteAutreDTO, or with status 404 (Not Found)
     */
    @GetMapping("/compte-autres/{id}")
    @Timed
    public ResponseEntity<CompteAutreDTO> getCompteAutre(@PathVariable Long id) {
        log.debug("REST request to get CompteAutre : {}", id);
        CompteAutreDTO compteAutreDTO = compteAutreService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(compteAutreDTO));
    }

    /**
     * DELETE  /compte-autres/:id : delete the "id" compteAutre.
     *
     * @param id the id of the compteAutreDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/compte-autres/{id}")
    @Timed
    public ResponseEntity<Void> deleteCompteAutre(@PathVariable Long id) {
        log.debug("REST request to delete CompteAutre : {}", id);
        compteAutreService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
