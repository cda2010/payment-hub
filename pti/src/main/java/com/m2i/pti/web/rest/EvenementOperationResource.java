package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.EvenementOperation;
import com.m2i.pti.service.EvenementOperationService;
import com.m2i.pti.service.dto.EvenementOperationDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing EvenementOperation.
 */
@RestController
@RequestMapping("/api")
public class EvenementOperationResource {

    private final Logger log = LoggerFactory.getLogger(EvenementOperationResource.class);

    private static final String ENTITY_NAME = "evenementOperation";

    private final EvenementOperationService evenementOperationService;

    public EvenementOperationResource(EvenementOperationService evenementOperationService) {
        this.evenementOperationService = evenementOperationService;
    }

    /**
     * POST  /evenement-operations : Create a new evenementOperation.
     *
     * @param evenementOperation the evenementOperation to create
     * @return the ResponseEntity with status 201 (Created) and with body the new evenementOperation, or with status 400 (Bad Request) if the evenementOperation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/evenement-operations")
    @Timed
    public ResponseEntity<EvenementOperation> createEvenementOperation(@Valid @RequestBody EvenementOperation evenementOperation) throws URISyntaxException {
        log.debug("REST request to save EvenementOperation : {}", evenementOperation);
        if (evenementOperation.getId() != null) {
            throw new BadRequestAlertException("A new evenementOperation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        EvenementOperation result = evenementOperationService.save(evenementOperation);
        return ResponseEntity.created(new URI("/api/evenement-operations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /evenement-operations : Updates an existing evenementOperation.
     *
     * @param evenementOperation the evenementOperation to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated evenementOperation,
     * or with status 400 (Bad Request) if the evenementOperation is not valid,
     * or with status 500 (Internal Server Error) if the evenementOperation couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/evenement-operations")
    @Timed
    public ResponseEntity<EvenementOperation> updateEvenementOperation(@Valid @RequestBody EvenementOperation evenementOperation) throws URISyntaxException {
        log.debug("REST request to update EvenementOperation : {}", evenementOperation);
        if (evenementOperation.getId() == null) {
            return createEvenementOperation(evenementOperation);
        }
        EvenementOperation result = evenementOperationService.save(evenementOperation);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, evenementOperation.getId().toString()))
            .body(result);
    }

    /**
     * GET  /evenement-operations : get all the evenementOperations.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of evenementOperations in body
     */
    @GetMapping("/evenement-operations-all")
    @Timed
    public List<EvenementOperation> getAllEvenementOperations() {
        log.debug("REST request to get all EvenementOperations");
        return evenementOperationService.findAll();
        }

    @GetMapping("/evenement-operations")
    @Timed
    public ResponseEntity<List<EvenementOperationDTO>> getAllEvenementOperations(Pageable pageable,@RequestParam(required = false) Long compteAutreId,
                                                                                 @RequestParam(required = false) ZonedDateTime dateDebut,
                                                                                 @RequestParam(required = false) ZonedDateTime dateFin,
                                                                                 @RequestParam(required = false) String telephone,
                                                                                 @RequestParam(required = false) Integer estLevee) {
        log.debug("REST request to get all EvenementOperations");
        Page<EvenementOperationDTO> page=evenementOperationService.findEvenementByCompteAutre(compteAutreId,dateDebut,dateFin,telephone,estLevee,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/evenement-operations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /evenement-operations/:id : get the "id" evenementOperation.
     *
     * @param id the id of the evenementOperation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the evenementOperation, or with status 404 (Not Found)
     */
    @GetMapping("/evenement-operations/{id}")
    @Timed
    public ResponseEntity<EvenementOperation> getEvenementOperation(@PathVariable Long id) {
        log.debug("REST request to get EvenementOperation : {}", id);
        EvenementOperation evenementOperation = evenementOperationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(evenementOperation));
    }

    /**
     * DELETE  /evenement-operations/:id : delete the "id" evenementOperation.
     *
     * @param id the id of the evenementOperation to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/evenement-operations/{id}")
    @Timed
    public ResponseEntity<Void> deleteEvenementOperation(@PathVariable Long id) {
        log.debug("REST request to delete EvenementOperation : {}", id);
        evenementOperationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/evenement/find-by-comptre-autre")
    @Timed
    public ResponseEntity<List<EvenementOperationDTO>> findEvenemntByCompteAutre(@RequestParam(required = false) Long compteAutreId, @RequestParam(required = false) ZonedDateTime dateDebut, @RequestParam(required = false) ZonedDateTime dateFin, @RequestParam(required = false) String telephone, @RequestParam(required = false) Integer estLevee, Pageable pageable){
        log.debug("REST request to get a page of Evenement by comptre autre");
        Page<EvenementOperationDTO> page=evenementOperationService.findEvenementByCompteAutre(compteAutreId,dateDebut,dateFin,telephone,estLevee,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/evenement-operations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("")
    @Timed
    public ResponseEntity<EvenementOperation> evenementClient(@RequestParam(required = false) Long operationId){
        log.debug("--------Request to create evenement operation of client ");
        EvenementOperation evenementOperation= evenementOperationService.evenementClient(operationId);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(evenementOperation));
    }
}
