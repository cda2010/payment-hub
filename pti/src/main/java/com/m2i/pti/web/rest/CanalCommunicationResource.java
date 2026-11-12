package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.CanalCommunication;
import com.m2i.pti.service.CanalCommunicationService;
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
 * REST controller for managing CanalCommunication.
 */
@RestController
@RequestMapping("/api")
public class CanalCommunicationResource {

    private final Logger log = LoggerFactory.getLogger(CanalCommunicationResource.class);

    private static final String ENTITY_NAME = "canalCommunication";

    private final CanalCommunicationService canalCommunicationService;

    public CanalCommunicationResource(CanalCommunicationService canalCommunicationService) {
        this.canalCommunicationService = canalCommunicationService;
    }

    /**
     * POST  /canal-communications : Create a new canalCommunication.
     *
     * @param canalCommunication the canalCommunication to create
     * @return the ResponseEntity with status 201 (Created) and with body the new canalCommunication, or with status 400 (Bad Request) if the canalCommunication has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/canal-communications")
    @Timed
    public ResponseEntity<CanalCommunication> createCanalCommunication(@RequestBody CanalCommunication canalCommunication) throws URISyntaxException {
        log.debug("REST request to save CanalCommunication : {}", canalCommunication);
        if (canalCommunication.getId() != null) {
            throw new BadRequestAlertException("A new canalCommunication cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CanalCommunication result = canalCommunicationService.save(canalCommunication);
        return ResponseEntity.created(new URI("/api/canal-communications/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /canal-communications : Updates an existing canalCommunication.
     *
     * @param canalCommunication the canalCommunication to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated canalCommunication,
     * or with status 400 (Bad Request) if the canalCommunication is not valid,
     * or with status 500 (Internal Server Error) if the canalCommunication couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/canal-communications")
    @Timed
    public ResponseEntity<CanalCommunication> updateCanalCommunication(@RequestBody CanalCommunication canalCommunication) throws URISyntaxException {
        log.debug("REST request to update CanalCommunication : {}", canalCommunication);
        if (canalCommunication.getId() == null) {
            return createCanalCommunication(canalCommunication);
        }
        CanalCommunication result = canalCommunicationService.save(canalCommunication);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, canalCommunication.getId().toString()))
            .body(result);
    }

    /**
     * GET  /canal-communications : get all the canalCommunications.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of canalCommunications in body
     */
    @GetMapping("/canal-communications")
    @Timed
    public List<CanalCommunication> getAllCanalCommunications() {
        log.debug("REST request to get all CanalCommunications");
        return canalCommunicationService.findAll();
        }

    /**
     * GET  /canal-communications/:id : get the "id" canalCommunication.
     *
     * @param id the id of the canalCommunication to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the canalCommunication, or with status 404 (Not Found)
     */
    @GetMapping("/canal-communications/{id}")
    @Timed
    public ResponseEntity<CanalCommunication> getCanalCommunication(@PathVariable Long id) {
        log.debug("REST request to get CanalCommunication : {}", id);
        CanalCommunication canalCommunication = canalCommunicationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(canalCommunication));
    }

    /**
     * DELETE  /canal-communications/:id : delete the "id" canalCommunication.
     *
     * @param id the id of the canalCommunication to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/canal-communications/{id}")
    @Timed
    public ResponseEntity<Void> deleteCanalCommunication(@PathVariable Long id) {
        log.debug("REST request to delete CanalCommunication : {}", id);
        canalCommunicationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
