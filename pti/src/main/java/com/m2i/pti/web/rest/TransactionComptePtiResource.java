package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.outils.util.ErrorException;
import com.m2i.pti.domain.SommeSolde;
import com.m2i.pti.service.TransactionComptePtiService;
import com.m2i.pti.service.dto.TransactionComptePtiDTO;
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
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing TransactionComptePti.
 */
@RestController
@RequestMapping("/api")
public class TransactionComptePtiResource {

    private final Logger log = LoggerFactory.getLogger(TransactionComptePtiResource.class);

    private static final String ENTITY_NAME = "transactionComptePti";

    private final TransactionComptePtiService transactionComptePtiService;

    public TransactionComptePtiResource(TransactionComptePtiService transactionComptePtiService) {
        this.transactionComptePtiService = transactionComptePtiService;
    }

    /**
     * POST  /transaction-compte-ptis : Create a new transactionComptePti.
     *
     * @param transactionComptePtiDTO the transactionComptePtiDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new transactionComptePtiDTO, or with status 400 (Bad Request) if the transactionComptePti has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/transaction-compte-ptis")
    @Timed
    public ResponseEntity<TransactionComptePtiDTO> createTransactionComptePti(@Valid @RequestBody TransactionComptePtiDTO transactionComptePtiDTO) throws URISyntaxException {
        log.debug("REST request to save TransactionComptePti : {}", transactionComptePtiDTO);
        if (transactionComptePtiDTO.getId() != null) {
            throw new BadRequestAlertException("A new transactionComptePti cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TransactionComptePtiDTO result = transactionComptePtiService.save(transactionComptePtiDTO);
        return ResponseEntity.created(new URI("/api/transaction-compte-ptis/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /transaction-compte-ptis : Updates an existing transactionComptePti.
     *
     * @param transactionComptePtiDTO the transactionComptePtiDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated transactionComptePtiDTO,
     * or with status 400 (Bad Request) if the transactionComptePtiDTO is not valid,
     * or with status 500 (Internal Server Error) if the transactionComptePtiDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/transaction-compte-ptis")
    @Timed
    public ResponseEntity<TransactionComptePtiDTO> updateTransactionComptePti(@Valid @RequestBody TransactionComptePtiDTO transactionComptePtiDTO) throws URISyntaxException {
        log.debug("REST request to update TransactionComptePti : {}", transactionComptePtiDTO);
        if (transactionComptePtiDTO.getId() == null) {
            return createTransactionComptePti(transactionComptePtiDTO);
        }
        TransactionComptePtiDTO result = transactionComptePtiService.save(transactionComptePtiDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, transactionComptePtiDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /transaction-compte-ptis : get all the transactionComptePtis.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of transactionComptePtis in body
     */
    @GetMapping("/transaction-compte-ptis")
    @Timed
    public ResponseEntity<List<TransactionComptePtiDTO>> getAllTransactionComptePtis(Pageable pageable) {
        log.debug("REST request to get a page of TransactionComptePtis");
        Page<TransactionComptePtiDTO> page = transactionComptePtiService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/transaction-compte-ptis");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /transaction-compte-ptis : get the transactionComptePtis by compteAutre.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of transactionComptePtis in body
     */
    @GetMapping("/transaction-compte-ptis/ByCompteAutre000000")
    @Timed
    public ResponseEntity<List<TransactionComptePtiDTO>> getTransactionComptePtisByCompteAutre000(@RequestParam Long compteAutreId, Pageable pageable, @RequestParam(required = false) ZonedDateTime dateDebut, @RequestParam(required = false) ZonedDateTime dateFin) {
        log.debug("REST request to get a page of TransactionComptePtis");
        Page<TransactionComptePtiDTO> page = transactionComptePtiService.findByCompteAutre(compteAutreId, dateDebut, dateFin, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/transaction-compte-ptis");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    @GetMapping("/transaction-compte-ptis/ByCompteAutre")
    @Timed
    public ResponseEntity<List<TransactionComptePtiDTO>> getTransactionComptePtisByCompteAutre(@RequestParam Long compteAutreId, Pageable pageable, @RequestParam(required = false) ZonedDateTime dateDebut, @RequestParam(required = false) ZonedDateTime dateFin) {
        log.debug("REST request to get a page of TransactionComptePtis");
        Page<TransactionComptePtiDTO> page = transactionComptePtiService.findByCompteAutre(compteAutreId, dateDebut, dateFin, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/transaction-compte-ptis");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    @GetMapping("/transaction-compte-ptis/ByCompteAutre-optimise")
    @Timed
    public ResponseEntity<List<TransactionComptePtiDTO>> getTransactionComptePtisByCompteAutreOptimise(@RequestParam Long compteAutreId, Pageable pageable, @RequestParam(required = false) ZonedDateTime dateDebut, @RequestParam(required = false) ZonedDateTime dateFin,@RequestParam(required = false) String jhiType) {
        log.debug("REST request to get a page of TransactionComptePtis");
        Page<TransactionComptePtiDTO> page = transactionComptePtiService.findByCompteAutreOptimise(compteAutreId, dateDebut, dateFin,jhiType, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/transaction-compte-ptis");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/transaction-compte-ptis/list/ByCompteAutre")
    @Timed
    public List<TransactionComptePtiDTO> getListTransactionComptePtisByCompteAutre(@RequestParam Long compteAutreId, @RequestParam(required = false) ZonedDateTime dateDebut, @RequestParam(required = false) ZonedDateTime dateFin) {
        log.debug("REST request to get a page of TransactionComptePtis");
        List<TransactionComptePtiDTO> list = transactionComptePtiService.findListByCompteAutre(compteAutreId, dateDebut, dateFin);
        return list;
    }

    /**
     * GET  /transaction-compte-ptis/:id : get the "id" transactionComptePti.
     *
     * @param id the id of the transactionComptePtiDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the transactionComptePtiDTO, or with status 404 (Not Found)
     */
    @GetMapping("/transaction-compte-ptis/{id}")
    @Timed
    public ResponseEntity<TransactionComptePtiDTO> getTransactionComptePti(@PathVariable Long id) {
        log.debug("REST request to get TransactionComptePti : {}", id);
        TransactionComptePtiDTO transactionComptePtiDTO = transactionComptePtiService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(transactionComptePtiDTO));
    }

    @GetMapping("/transaction-compte-ptis/bilanTransaction")
    @Timed
    public ResponseEntity<BigDecimal> getTransactionComptePtiBilan(@RequestParam Long compteAutreId) {
        return  ResponseUtil.wrapOrNotFound(Optional.ofNullable(transactionComptePtiService.bilanTransactionByCompte(compteAutreId)));
    }

    /**
     * DELETE  /transaction-compte-ptis/:id : delete the "id" transactionComptePti.
     *
     * @param id the id of the transactionComptePtiDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/transaction-compte-ptis/{id}")
    @Timed
    public ResponseEntity<Void> deleteTransactionComptePti(@PathVariable Long id) {
        log.debug("REST request to delete TransactionComptePti : {}", id);
        transactionComptePtiService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/findByOperation")
    public ResponseEntity<List<TransactionComptePtiDTO>> getTransactionByOperation( @RequestParam(required = true) Long  operationId,Pageable pageable) {
        Page<TransactionComptePtiDTO> page = transactionComptePtiService.findByOperation(operationId,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/operation");
        return new ResponseEntity<>(page.getContent(),headers,HttpStatus.OK);
    }

    @GetMapping("/transaction-compte-ptis/searchxx/{id}")
    @Timed
    public List<TransactionComptePtiDTO> getTransactionComptePtiAll(@PathVariable Long id) throws ErrorException {
        log.debug("REST request to get TransactionComptePti : {}", id);
        return transactionComptePtiService.findByOperationId(id);
    }
    @GetMapping("/findByOperation-optimise")
    public ResponseEntity<List<TransactionComptePtiDTO>> getTransactionByOperationOptimise( @RequestParam(required = true) Long  operationId,Pageable pageable) {
        Page<TransactionComptePtiDTO> page = transactionComptePtiService.findByOperationOptimise(operationId,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/operation");
        return new ResponseEntity<>(page.getContent(),headers,HttpStatus.OK);
    }


/*    @GetMapping("/solde-clients")
    @Timed
    public SommeSolde getSommeSoldeClients(@RequestParam(required = false) Long pereUoId) {
        return transactionComptePtiService.getSommeSoldeClients(pereUoId);
    }
    @GetMapping("/solde-commission")
    @Timed
    public SommeSolde getSommeSoldeCommission(@RequestParam(required = false) Long pereUoId) {
        return transactionComptePtiService.getSommeSoldeCommission(pereUoId);
    }
    @GetMapping("/solde-commission-att")
    @Timed
    public SommeSolde getSommeSoldeCommissionAtt(@RequestParam(required = false) Long pereUoId) {
        return transactionComptePtiService.getSommeSoldeCommissionAtt(pereUoId);
    }
    @GetMapping("/solde-taxe")
    @Timed
    public SommeSolde getSommeSoldeTaxe(@RequestParam(required = false) Long pereUoId) {
        return transactionComptePtiService.getSoldeTaxe(pereUoId);
    }
    @GetMapping("/solde-transfert")
    @Timed
    public SommeSolde getSommeSoldeTransfert(@RequestParam(required = false) Long pereUoId) {
        return transactionComptePtiService.getSoldeTransfert(pereUoId);
    }
    @GetMapping("/solde-agent/{agent}")
    @Timed
    public SommeSolde getSoldeAgent(@PathVariable String agent) {
        return transactionComptePtiService.getSoldeAgent(agent);
    }
    @GetMapping("/solde-plate-forme")
    @Timed
    public SommeSolde getSommeSoldePlateForme(@RequestParam(required = false) Long pereUoId) {
        return transactionComptePtiService.getSommeSoldePlateForme(pereUoId);
    }*/

}
