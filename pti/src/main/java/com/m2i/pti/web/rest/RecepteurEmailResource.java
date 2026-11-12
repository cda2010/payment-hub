package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.RecepteurEmail;
import com.m2i.pti.service.RecepteurEmailService;
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
 * REST controller for managing RecepteurEmail.
 */
@RestController
@RequestMapping("/api")
public class RecepteurEmailResource {

    private final Logger log = LoggerFactory.getLogger(RecepteurEmailResource.class);

    private static final String ENTITY_NAME = "recepteurEmail";

    private final RecepteurEmailService recepteurEmailService;

    public RecepteurEmailResource(RecepteurEmailService recepteurEmailService) {
        this.recepteurEmailService = recepteurEmailService;
    }

    /**
     * POST  /recepteur-emails : Create a new recepteurEmail.
     *
     * @param recepteurEmail the recepteurEmail to create
     * @return the ResponseEntity with status 201 (Created) and with body the new recepteurEmail, or with status 400 (Bad Request) if the recepteurEmail has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/recepteur-emails")
    @Timed
    public ResponseEntity<RecepteurEmail> createRecepteurEmail(@RequestBody RecepteurEmail recepteurEmail) throws URISyntaxException {
        log.debug("REST request to save RecepteurEmail : {}", recepteurEmail);
        if (recepteurEmail.getId() != null) {
            throw new BadRequestAlertException("A new recepteurEmail cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RecepteurEmail result = recepteurEmailService.save(recepteurEmail);
        return ResponseEntity.created(new URI("/api/recepteur-emails/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /recepteur-emails : Updates an existing recepteurEmail.
     *
     * @param recepteurEmail the recepteurEmail to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated recepteurEmail,
     * or with status 400 (Bad Request) if the recepteurEmail is not valid,
     * or with status 500 (Internal Server Error) if the recepteurEmail couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/recepteur-emails")
    @Timed
    public ResponseEntity<RecepteurEmail> updateRecepteurEmail(@RequestBody RecepteurEmail recepteurEmail) throws URISyntaxException {
        log.debug("REST request to update RecepteurEmail : {}", recepteurEmail);
        if (recepteurEmail.getId() == null) {
            return createRecepteurEmail(recepteurEmail);
        }
        RecepteurEmail result = recepteurEmailService.save(recepteurEmail);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, recepteurEmail.getId().toString()))
            .body(result);
    }

    /**
     * GET  /recepteur-emails : get all the recepteurEmails.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of recepteurEmails in body
     */
    @GetMapping("/recepteur-emails")
    @Timed
    public List<RecepteurEmail> getAllRecepteurEmails() {
        log.debug("REST request to get all RecepteurEmails");
        return recepteurEmailService.findAll();
        }

    /**
     * GET  /recepteur-emails/:id : get the "id" recepteurEmail.
     *
     * @param id the id of the recepteurEmail to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the recepteurEmail, or with status 404 (Not Found)
     */
    @GetMapping("/recepteur-emails/{id}")
    @Timed
    public ResponseEntity<RecepteurEmail> getRecepteurEmail(@PathVariable Long id) {
        log.debug("REST request to get RecepteurEmail : {}", id);
        RecepteurEmail recepteurEmail = recepteurEmailService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(recepteurEmail));
    }

    /**
     * DELETE  /recepteur-emails/:id : delete the "id" recepteurEmail.
     *
     * @param id the id of the recepteurEmail to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/recepteur-emails/{id}")
    @Timed
    public ResponseEntity<Void> deleteRecepteurEmail(@PathVariable Long id) {
        log.debug("REST request to delete RecepteurEmail : {}", id);
        recepteurEmailService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
