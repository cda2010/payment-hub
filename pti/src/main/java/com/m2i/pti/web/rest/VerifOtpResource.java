package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.VerifOtp;
import com.m2i.pti.service.VerifOtpService;
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

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing VerifOtp.
 */
@RestController
@RequestMapping("/api")
public class VerifOtpResource {

    private final Logger log = LoggerFactory.getLogger(VerifOtpResource.class);

    private static final String ENTITY_NAME = "verifOtp";

    private final VerifOtpService verifOtpService;

    public VerifOtpResource(VerifOtpService verifOtpService) {
        this.verifOtpService = verifOtpService;
    }

    /**
     * POST  /verif-otps : Create a new verifOtp.
     *
     * @param verifOtp the verifOtp to create
     * @return the ResponseEntity with status 201 (Created) and with body the new verifOtp, or with status 400 (Bad Request) if the verifOtp has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/verif-otps")
    @Timed
    public ResponseEntity<VerifOtp> createVerifOtp(@RequestBody VerifOtp verifOtp) throws URISyntaxException {
        log.debug("REST request to save VerifOtp : {}", verifOtp);
        if (verifOtp.getId() != null) {
            throw new BadRequestAlertException("A new verifOtp cannot already have an ID", ENTITY_NAME, "idexists");
        }
        VerifOtp result = verifOtpService.save(verifOtp);
        return ResponseEntity.created(new URI("/api/verif-otps/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /verif-otps : Updates an existing verifOtp.
     *
     * @param verifOtp the verifOtp to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated verifOtp,
     * or with status 400 (Bad Request) if the verifOtp is not valid,
     * or with status 500 (Internal Server Error) if the verifOtp couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/verif-otps")
    @Timed
    public ResponseEntity<VerifOtp> updateVerifOtp(@RequestBody VerifOtp verifOtp) throws URISyntaxException {
        log.debug("REST request to update VerifOtp : {}", verifOtp);
        if (verifOtp.getId() == null) {
            return createVerifOtp(verifOtp);
        }
        VerifOtp result = verifOtpService.save(verifOtp);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, verifOtp.getId().toString()))
            .body(result);
    }

    /**
     * GET  /verif-otps : get all the verifOtps.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of verifOtps in body
     */
    @GetMapping("/verif-otps")
    @Timed
    public ResponseEntity<List<VerifOtp>> getAllVerifOtps(Pageable pageable) {
        log.debug("REST request to get a page of VerifOtps");
        Page<VerifOtp> page = verifOtpService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/verif-otps");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /verif-otps/:id : get the "id" verifOtp.
     *
     * @param id the id of the verifOtp to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the verifOtp, or with status 404 (Not Found)
     */
    @GetMapping("/verif-otps/{id}")
    @Timed
    public ResponseEntity<VerifOtp> getVerifOtp(@PathVariable Long id) {
        log.debug("REST request to get VerifOtp : {}", id);
        VerifOtp verifOtp = verifOtpService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(verifOtp));
    }

    /**
     * DELETE  /verif-otps/:id : delete the "id" verifOtp.
     *
     * @param id the id of the verifOtp to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/verif-otps/{id}")
    @Timed
    public ResponseEntity<Void> deleteVerifOtp(@PathVariable Long id) {
        log.debug("REST request to delete VerifOtp : {}", id);
        verifOtpService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
