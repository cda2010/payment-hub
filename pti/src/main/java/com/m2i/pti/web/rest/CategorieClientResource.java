package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.CategorieClient;
import com.m2i.pti.repository.CategorieClientRepository;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing CategorieClient.
 */
@RestController
@RequestMapping("/api")
public class CategorieClientResource {

    private final Logger log = LoggerFactory.getLogger(CategorieClientResource.class);

    private static final String ENTITY_NAME = "categorieClient";


    private final CategorieClientRepository categorieClientRepository;

    public CategorieClientResource(CategorieClientRepository categorieClientRepository) {
        this.categorieClientRepository = categorieClientRepository;

    }

    /**
     * POST  /categorie-clients : Create a new categorieClient.
     *
     * @param categorieClient the categorieClient to create
     * @return the ResponseEntity with status 201 (Created) and with body the new categorieClient, or with status 400 (Bad Request) if the categorieClient has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/categorie-clients")
    @Timed
    public ResponseEntity<CategorieClient> createCategorieClient(@RequestBody CategorieClient categorieClient) throws URISyntaxException {
        log.debug("REST request to save CategorieClient : {}", categorieClient);
        if (categorieClient.getId() != null) {
            throw new BadRequestAlertException("A new categorieClient cannot already have an ID", ENTITY_NAME, "idexists");
        }

        CategorieClient result = categorieClientRepository.save(categorieClient);

        return ResponseEntity.created(new URI("/api/categorie-clients/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /categorie-clients : Updates an existing categorieClient.
     *
     * @param categorieClient the categorieClient to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated categorieClient,
     * or with status 400 (Bad Request) if the categorieClient is not valid,
     * or with status 500 (Internal Server Error) if the categorieClient couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/categorie-clients")
    @Timed
    public ResponseEntity<CategorieClient> updateCategorieClient(@RequestBody CategorieClient categorieClient) throws URISyntaxException {
        log.debug("REST request to update CategorieClient : {}", categorieClient);
        if (categorieClient.getId() == null) {
            return createCategorieClient(categorieClient);
        }

        CategorieClient result = categorieClientRepository.save(categorieClient);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, categorieClient.getId().toString()))
            .body(result);
    }

    /**
     * GET  /categorie-clients : get all the categorieClients.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of categorieClients in body
     */
    @GetMapping("/categorie-clients")
    @Timed

    public List<CategorieClient> getAllCategorieClients() {
        log.debug("REST request to get all CategorieClients");
        return categorieClientRepository.findAll();
        }


    /**
     * GET  /categorie-clients/:id : get the "id" categorieClient.
     *
     * @param id the id of the categorieClient to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the categorieClient, or with status 404 (Not Found)
     */
    @GetMapping("/categorie-clients/{id}")
    @Timed
    public ResponseEntity<CategorieClient> getCategorieClient(@PathVariable Long id) {
        log.debug("REST request to get CategorieClient : {}", id);

        CategorieClient categorieClient = categorieClientRepository.findById(id).orElse(null);

        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(categorieClient));
    }

    /**
     * DELETE  /categorie-clients/:id : delete the "id" categorieClient.
     *
     * @param id the id of the categorieClient to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/categorie-clients/{id}")
    @Timed
    public ResponseEntity<Void> deleteCategorieClient(@PathVariable Long id) {
        log.debug("REST request to delete CategorieClient : {}", id);

        categorieClientRepository.deleteById(id);

        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
