package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.Oper;
import com.m2i.omnicanal.application.repository.OperationRepository;
import com.m2i.omnicanal.application.service.OperationService;
import com.m2i.omnicanal.application.service.dto.DataChartResponse;
import com.m2i.omnicanal.application.service.dto.OperationDTO;

import com.m2i.omnicanal.model.Operation;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Operation.
 */
@RestController
@RequestMapping("/api")
public class OperationResource {

    private final Logger log = LoggerFactory.getLogger(OperationResource.class);

    private static final String ENTITY_NAME = "operation";

    private final OperationService operationService;

    private final OperationRepository operationRepository;

    public OperationResource(OperationService operationService, OperationRepository operationRepository) {
        this.operationService = operationService;
        this.operationRepository = operationRepository;
    }

    /**
     * POST /operations : Create a new operation.
     *
     * @param operation the operation to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new operation, or with status 400 (Bad Request) if the operation has
     * already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/operations")
    @Timed
    public ResponseEntity<Oper> createOperation(@RequestBody Oper operation) throws URISyntaxException {
        log.debug("REST request to save Operation : {}", operation);
        if (operation.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new operation cannot already have an ID")).body(null);
        }
        Oper result = operationService.save(operation);
        return ResponseEntity.created(new URI("/api/operations/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT /operations : Updates an existing operation.
     *
     * @param operation the operation to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated
     * operation, or with status 400 (Bad Request) if the operation is not
     * valid, or with status 500 (Internal Server Error) if the operation
     * couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/operations")
    @Timed
    public ResponseEntity<Oper> updateOperation(@RequestBody Oper operation) throws URISyntaxException {
        log.debug("REST request to update Operation : {}", operation);
        if (operation.getId() == null) {
            return createOperation(operation);
        }
        Oper result = operationService.save(operation);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, operation.getId().toString()))
                .body(result);
    }

    /**
     * GET /operations : get all the operations.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of
     * operations in body
     */
    @GetMapping("/operations")
    @Timed
    public List<OperationDTO> getAllOperations() {
        log.debug("REST request to get all Operations");
        return operationService.findAll();
    }

    /**
     * GET /operations/:id : get the "id" operation.
     *
     * @param id the id of the operation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the
     * operation, or with status 404 (Not Found)
     */
    @GetMapping("/operations/{id}")
    @Timed
    public ResponseEntity<Oper> getOperation(@PathVariable Long id) {
        log.debug("REST request to get Operation : {}", id);
        Oper operation = operationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(operation));
    }

    /**
     * DELETE /operations/:id : delete the "id" operation.
     *
     * @param id the id of the operation to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/operations/{id}")
    @Timed
    public ResponseEntity<Void> deleteOperation(@PathVariable Long id) {
        log.debug("REST request to delete Operation : {}", id);
        operationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    @GetMapping("/operations/filtered")
    @Timed
    public ResponseEntity<List<OperationDTO>> getAllFilteredOperations(@RequestParam(required = false) String pnumero,
            @RequestParam(required = false) Long ptypeServiceId,
            @RequestParam(required = false) String pfseSrc,
            @RequestParam(required = false) String pfseDest,
            @RequestParam(required = false) ZonedDateTime pdebut,
            @RequestParam(required = false) ZonedDateTime pfin) {
        log.debug("\n\n\n-------------------------------: {},{},{},{},{},{}", ptypeServiceId, pnumero, pfseSrc, pfseDest, pdebut, pfin);
        if (pfin != null) {
            pfin = pfin.plusDays(1);
        }
        List<OperationDTO> listOp = operationRepository.rechercheOperation(ptypeServiceId, pnumero, pfseSrc, pfseDest, pdebut, pfin);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(listOp));

    }
     @GetMapping("/operations/top10")
    @Timed
    public ResponseEntity<List<OperationDTO>> getTop10FilteredHistoriqueCall() {
      
        List<OperationDTO> listOp = operationRepository.findTop10Operation();
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(listOp));

    }
   
}
