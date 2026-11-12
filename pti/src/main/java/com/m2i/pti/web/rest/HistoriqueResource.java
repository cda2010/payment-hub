package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Historique;
import com.m2i.pti.service.HistoriqueService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
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
 * REST controller for managing Historique.
 */
@RestController
@RequestMapping("/api")
public class HistoriqueResource {

    private final Logger log = LoggerFactory.getLogger(HistoriqueResource.class);

    private static final String ENTITY_NAME = "historique";

    private final HistoriqueService historiqueService;

    public HistoriqueResource(HistoriqueService historiqueService) {
        this.historiqueService = historiqueService;
    }

    /**
     * POST  /historiques : Create a new historique.
     *
     * @param historique the historique to create
     * @return the ResponseEntity with status 201 (Created) and with body the new historique, or with status 400 (Bad Request) if the historique has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/historiques")
    @Timed
    public ResponseEntity<Historique> createHistorique(@RequestBody Historique historique) throws URISyntaxException {
        log.debug("REST request to save Historique : {}", historique);
        if (historique.getId() != null) {
            throw new BadRequestAlertException("A new historique cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Historique result = historiqueService.save(historique);
        return ResponseEntity.created(new URI("/api/historiques/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /historiques : Updates an existing historique.
     *
     * @param historique the historique to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated historique,
     * or with status 400 (Bad Request) if the historique is not valid,
     * or with status 500 (Internal Server Error) if the historique couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/historiques")
    @Timed
    public ResponseEntity<Historique> updateHistorique(@RequestBody Historique historique) throws URISyntaxException {
        log.debug("REST request to update Historique : {}", historique);
        if (historique.getId() == null) {
            return createHistorique(historique);
        }
        Historique result = historiqueService.save(historique);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, historique.getId().toString()))
            .body(result);
    }

    /**
     * GET  /historiques : get all the historiques.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of historiques in body
     */
    @GetMapping("/historiques")
    @Timed
    public ResponseEntity<List<Historique>> getAllHistoriques(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of Historiques");
        Page<Historique> page = historiqueService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/historiques");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /historiques/:id : get the "id" historique.
     *
     * @param id the id of the historique to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the historique, or with status 404 (Not Found)
     */
    @GetMapping("/historiques/{id}")
    @Timed
    public ResponseEntity<Historique> getHistorique(@PathVariable Long id) {
        log.debug("REST request to get Historique : {}", id);
        Historique historique = historiqueService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(historique));
    }

    /**
     * DELETE  /historiques/:id : delete the "id" historique.
     *
     * @param id the id of the historique to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/historiques/{id}")
    @Timed
    public ResponseEntity<Void> deleteHistorique(@PathVariable Long id) {
        log.debug("REST request to delete Historique : {}", id);
        historiqueService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/testCronJob")
    @Timed
    public void getHistoriqueTest() {
        historiqueService.historiqueSoldeClient();
    }
}
