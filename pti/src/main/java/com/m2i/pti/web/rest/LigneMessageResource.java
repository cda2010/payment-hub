package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.LigneMessage;
import com.m2i.pti.service.ChargementMessageService;
import com.m2i.pti.service.LigneMessageService;
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
 * REST controller for managing LigneMessage.
 */
@RestController
@RequestMapping("/api")
public class LigneMessageResource {

    private final Logger log = LoggerFactory.getLogger(LigneMessageResource.class);

    private static final String ENTITY_NAME = "ligneMessage";

    private final LigneMessageService ligneMessageService;

    private final ChargementMessageService chargementMessageService;

    public LigneMessageResource(LigneMessageService ligneMessageService, ChargementMessageService chargementMessageService) {
        this.ligneMessageService = ligneMessageService;
        this.chargementMessageService = chargementMessageService;
    }

    /**
     * POST  /ligne-messages : Create a new ligneMessage.
     *
     * @param ligneMessage the ligneMessage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ligneMessage, or with status 400 (Bad Request) if the ligneMessage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/ligne-messages")
    @Timed
    public ResponseEntity<LigneMessage> createLigneMessage(@RequestBody LigneMessage ligneMessage) throws URISyntaxException {
        log.debug("REST request to save LigneMessage : {}", ligneMessage);
        if (ligneMessage.getId() != null) {
            throw new BadRequestAlertException("A new ligneMessage cannot already have an ID", ENTITY_NAME, "idexists");
        }
        LigneMessage result = ligneMessageService.save(ligneMessage);
        return ResponseEntity.created(new URI("/api/ligne-messages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /ligne-messages : Updates an existing ligneMessage.
     *
     * @param ligneMessage the ligneMessage to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ligneMessage,
     * or with status 400 (Bad Request) if the ligneMessage is not valid,
     * or with status 500 (Internal Server Error) if the ligneMessage couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/ligne-messages")
    @Timed
    public ResponseEntity<LigneMessage> updateLigneMessage(@RequestBody LigneMessage ligneMessage) throws URISyntaxException {
        log.debug("REST request to update LigneMessage : {}", ligneMessage);
        if (ligneMessage.getId() == null) {
            return createLigneMessage(ligneMessage);
        }
        LigneMessage result = ligneMessageService.save(ligneMessage);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ligneMessage.getId().toString()))
            .body(result);
    }

    /**
     * GET  /ligne-messages : get all the ligneMessages.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of ligneMessages in body
     */
    @GetMapping("/ligne-messages")
    @Timed
    public ResponseEntity<List<LigneMessage>> getAllLigneMessages(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of LigneMessages");
        Page<LigneMessage> page = ligneMessageService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/ligne-messages");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /ligne-messages/:id : get the "id" ligneMessage.
     *
     * @param id the id of the ligneMessage to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ligneMessage, or with status 404 (Not Found)
     */
    @GetMapping("/ligne-messages/{id}")
    @Timed
    public ResponseEntity<LigneMessage> getLigneMessage(@PathVariable Long id) {
        log.debug("REST request to get LigneMessage : {}", id);
        LigneMessage ligneMessage = ligneMessageService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ligneMessage));
    }

    /**
     * DELETE  /ligne-messages/:id : delete the "id" ligneMessage.
     *
     * @param id the id of the ligneMessage to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/ligne-messages/{id}")
    @Timed
    public ResponseEntity<Void> deleteLigneMessage(@PathVariable Long id) {
        log.debug("REST request to delete LigneMessage : {}", id);
        ligneMessageService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/findByChargementId")
    public ResponseEntity<List<LigneMessage>> findByChargementId(@RequestParam Long chargementId,Pageable pageable) {

        Page<LigneMessage> page=ligneMessageService.getLigneMessagePage(chargementId,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/chargement-message");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);

    }

    @GetMapping("/deleteChargement")
    @Timed
    public Integer finById(@RequestParam Long id) {
        Integer nbr= ligneMessageService.findById(id);

        if (nbr > 0 )
        {
            ligneMessageService.deleteAll(id);
            return 0;
        }
        else {

            chargementMessageService.delete(id);
            return 1;
        }
    }
}
