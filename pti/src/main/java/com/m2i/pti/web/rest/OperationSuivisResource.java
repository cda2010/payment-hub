package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.OperationSuivis;
import com.m2i.pti.service.OperationSuivisService;
import com.m2i.pti.service.dto.OperationSuivisDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
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
 * REST controller for managing OperationSuivis.
 */
@RestController
@RequestMapping("/api")
public class OperationSuivisResource {

    private final Logger log = LoggerFactory.getLogger(OperationSuivisResource.class);

    private static final String ENTITY_NAME = "operationSuivis";

    private final OperationSuivisService operationSuivisService;

    public OperationSuivisResource(OperationSuivisService operationSuivisService) {
        this.operationSuivisService = operationSuivisService;
    }

    /**
     * POST  /operation-suivis : Create a new operationSuivis.
     *
     * @param operationSuivisDTO the operationSuivisDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new operationSuivisDTO, or with status 400 (Bad Request) if the operationSuivis has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/operation-suivis")
    @Timed
    public ResponseEntity<OperationSuivisDTO> createOperationSuivis(@RequestBody OperationSuivisDTO operationSuivisDTO) throws URISyntaxException {
        log.debug("REST request to save OperationSuivis : {}", operationSuivisDTO);
        if (operationSuivisDTO.getId() != null) {
            throw new BadRequestAlertException("A new operationSuivis cannot already have an ID", ENTITY_NAME, "idexists");
        }
        OperationSuivisDTO result = operationSuivisService.save(operationSuivisDTO);
        return ResponseEntity.created(new URI("/api/operation-suivis/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /operation-suivis : Updates an existing operationSuivis.
     *
     * @param operationSuivisDTO the operationSuivisDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated operationSuivisDTO,
     * or with status 400 (Bad Request) if the operationSuivisDTO is not valid,
     * or with status 500 (Internal Server Error) if the operationSuivisDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/operation-suivis")
    @Timed
    public ResponseEntity<OperationSuivisDTO> updateOperationSuivis(@RequestBody OperationSuivisDTO operationSuivisDTO) throws URISyntaxException {
        log.debug("REST request to update OperationSuivis : {}", operationSuivisDTO);
        if (operationSuivisDTO.getId() == null) {
            return createOperationSuivis(operationSuivisDTO);
        }
        OperationSuivisDTO result = operationSuivisService.save(operationSuivisDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, operationSuivisDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /operation-suivis : get all the operationSuivis.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of operationSuivis in body
     */
    @GetMapping("/operation-suivis")
    @Timed
    public ResponseEntity<List<OperationSuivisDTO>> getAllOperationSuivis(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of OperationSuivis");
        Page<OperationSuivisDTO> page = operationSuivisService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/operation-suivis");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /operation-suivis/:id : get the "id" operationSuivis.
     *
     * @param id the id of the operationSuivisDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the operationSuivisDTO, or with status 404 (Not Found)
     */
    @GetMapping("/operation-suivis/{id}")
    @Timed
    public ResponseEntity<OperationSuivisDTO> getOperationSuivis(@PathVariable Long id) {
        log.debug("REST request to get OperationSuivis : {}", id);
        OperationSuivisDTO operationSuivisDTO = operationSuivisService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(operationSuivisDTO));
    }

    /**
     * DELETE  /operation-suivis/:id : delete the "id" operationSuivis.
     *
     * @param id the id of the operationSuivisDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/operation-suivis/{id}")
    @Timed
    public ResponseEntity<Void> deleteOperationSuivis(@PathVariable Long id) {
        log.debug("REST request to delete OperationSuivis : {}", id);
        operationSuivisService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    @GetMapping("/findByNom/{nom}")
    @Timed
    public List<Client> findByNom(@PathVariable String nom){
        return operationSuivisService.findByNom(nom);
    }

    @GetMapping("/findByPrenom/{prenom}")
    @Timed
    public List<Client> findByPrenom(@PathVariable String prenom){
        return operationSuivisService.findByPrenom(prenom);
    }

    @GetMapping("/findByCodeAndNom/{code}/{nom}")
    @Timed
    public List<Client> findByCodeAndNom(@PathVariable("code") String code, @PathVariable("nom") String nom){
        return operationSuivisService.findByCodeAndNom(code,nom);
    }

    @GetMapping("/findByTelAndNom/{tel}/{nom}")
    @Timed
    public List<Client> findByTelAndNom(@PathVariable("tel") String tel, @PathVariable("nom") String nom){
        return operationSuivisService.findByTelAndNom(tel,nom);
    }

    @GetMapping("/findByCodeAndTelAndNom/{code}/{tel}/{nom}")
    @Timed
    public List<Client> findByCodeAndTelAndNom(@PathVariable("code") String code, @PathVariable("tel") String tel , @PathVariable("nom") String nom){
        return operationSuivisService.findByCodeAndTelAndNom(code,tel,nom);
    }
    @GetMapping("/findOperaSuivisByClient")
    @Timed
    public ResponseEntity<List<OperationSuivis>> findOperaSuivisByClient(@RequestParam(required = false) Long id,@ApiParam Pageable pageable){
        Page<OperationSuivis> page =operationSuivisService.findOperaSuivisByClient(id,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/operation-suivis");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
