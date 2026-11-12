package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.CommissionService;
import com.m2i.pti.service.dto.CommissionDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Commission.
 */
@RestController
@RequestMapping("/api")
public class CommissionResource {

    private final Logger log = LoggerFactory.getLogger(CommissionResource.class);

    private static final String ENTITY_NAME = "commission";

    private final CommissionService commissionService;


    public CommissionResource(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    /**
     * POST  /commissions : Create a new commission.
     *
     * @param commissionDTO the commissionDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new commissionDTO, or with status 400 (Bad Request) if the commission has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/commissions")
    @Timed
    public ResponseEntity<CommissionDTO> createCommission(@RequestBody CommissionDTO commissionDTO) throws URISyntaxException {
        log.debug("REST request to save Commission : {}", commissionDTO);
        if (commissionDTO.getId() != null) {
            throw new BadRequestAlertException("A new commission cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CommissionDTO result = commissionService.save(commissionDTO);
        return ResponseEntity.created(new URI("/api/commissions/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /commissions : Updates an existing commission.
     *
     * @param commissionDTO the commissionDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated commissionDTO,
     * or with status 400 (Bad Request) if the commissionDTO is not valid,
     * or with status 500 (Internal Server Error) if the commissionDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/commissions")
    @Timed
    public ResponseEntity<CommissionDTO> updateCommission(@RequestBody CommissionDTO commissionDTO) throws URISyntaxException {
        log.debug("REST request to update Commission : {}", commissionDTO);
        if (commissionDTO.getId() == null) {
            return createCommission(commissionDTO);
        }
        CommissionDTO result = commissionService.save(commissionDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, commissionDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /commissions : get all the commissions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of commissions in body
     */
    @GetMapping("/commissions")
    @Timed
    public ResponseEntity<List<CommissionDTO>> getAllCommissions(Pageable pageable, @RequestParam(required = false) Long uoId, @RequestParam(required = false) Boolean estPaye, @RequestParam(required = false) ZonedDateTime dateEnregistrement,@RequestParam(required = false) ZonedDateTime dateFin) {
        log.debug("REST request to get a page of Commissions"+dateFin);
        Page<CommissionDTO> page = commissionService.findAll(pageable, uoId, estPaye, dateEnregistrement,dateFin);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/commissions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/commissions/download")
    @Timed
    public void getAllCommissions(Pageable pageable, @RequestParam(required = false) Long uoId,
                                  @RequestParam(required = false) Boolean estPaye,
                                  @RequestParam(required = false) ZonedDateTime dateEnregistrement,
                                  HttpServletResponse response) throws IOException {

        InputStream stream = commissionService.getDatasOptimise(pageable, uoId, estPaye, dateEnregistrement);
        response.setContentType("application/csv");
        response.setHeader("Content-disposition", "attachment;filename=\"" + this.commissionService.getPathname() + "\"");
        response.setCharacterEncoding("UTF-8");
        IOUtils.copy(stream, response.getOutputStream());
        stream.close();
    }

    @GetMapping("/commissions/test_comptabilisation_frais_acq")
    @Timed
    public List<ResponseEntity> testComptabilisationFraisAcq() {
        return commissionService.repartitionCommissionAcq();
    }

    /**
     * GET  /commissions/:id : get the "id" commission.
     *
     * @param id the id of the commissionDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the commissionDTO, or with status 404 (Not Found)
     */
    @GetMapping("/commissions/{id}")
    @Timed
    public ResponseEntity<CommissionDTO> getCommission(@PathVariable Long id) {
        log.debug("REST request to get Commission : {}", id);
        CommissionDTO commissionDTO = commissionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(commissionDTO));
    }

    /**
     * DELETE  /commissions/:id : delete the "id" commission.
     *
     * @param id the id of the commissionDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/commissions/{id}")
    @Timed
    public ResponseEntity<Void> deleteCommission(@PathVariable Long id) {
        log.debug("REST request to delete Commission : {}", id);
        commissionService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    @GetMapping("/operation-commission")
    @Timed
    public ResponseEntity<List<CommissionDTO>> getCommissionOfOperation(@RequestParam Long operationId, Pageable pageable) {
        log.debug("Request to get Commission of operation");
        Page<CommissionDTO> page = commissionService.getCommissionOfOperation(operationId,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/operation");
        return new ResponseEntity<>(page.getContent(),headers,HttpStatus.OK);
    }

    @GetMapping("/commissions-optimise")
    @Timed
    public ResponseEntity<List<CommissionDTO>> getAllCommissionsOptimise(Pageable pageable, @RequestParam(required = false) Long uoId, @RequestParam(required = false) Boolean estPaye, @RequestParam(required = false) ZonedDateTime dateEnregistrement,@RequestParam(required = false) ZonedDateTime dateFin) {
        log.debug("REST request to get a page of Commissions"+dateFin);
        Page<CommissionDTO> page = commissionService.findAllOptimise(pageable, uoId, estPaye, dateEnregistrement,dateFin);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/commissions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);

    }

}
