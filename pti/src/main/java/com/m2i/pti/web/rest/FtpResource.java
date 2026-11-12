package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Ftp;
import com.m2i.pti.service.FtpService;
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
 * REST controller for managing Ftp.
 */
@RestController
@RequestMapping("/api")
public class FtpResource {

    private final Logger log = LoggerFactory.getLogger(FtpResource.class);

    private static final String ENTITY_NAME = "ftp";

    private final FtpService ftpService;

    public FtpResource(FtpService ftpService) {
        this.ftpService = ftpService;
    }

    /**
     * POST  /ftps : Create a new ftp.
     *
     * @param ftp the ftp to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ftp, or with status 400 (Bad Request) if the ftp has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/ftps")
    @Timed
    public ResponseEntity<Ftp> createFtp(@RequestBody Ftp ftp) throws URISyntaxException {
        log.debug("REST request to save Ftp : {}", ftp);
        if (ftp.getId() != null) {
            throw new BadRequestAlertException("A new ftp cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Ftp result = ftpService.save(ftp);
        return ResponseEntity.created(new URI("/api/ftps/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /ftps : Updates an existing ftp.
     *
     * @param ftp the ftp to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ftp,
     * or with status 400 (Bad Request) if the ftp is not valid,
     * or with status 500 (Internal Server Error) if the ftp couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/ftps")
    @Timed
    public ResponseEntity<Ftp> updateFtp(@RequestBody Ftp ftp) throws URISyntaxException {
        log.debug("REST request to update Ftp : {}", ftp);
        if (ftp.getId() == null) {
            return createFtp(ftp);
        }
        Ftp result = ftpService.save(ftp);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ftp.getId().toString()))
            .body(result);
    }

    /**
     * GET  /ftps : get all the ftps.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of ftps in body
     */
    @GetMapping("/ftps")
    @Timed
    public ResponseEntity<List<Ftp>> getAllFtps(Pageable pageable) {
        log.debug("REST request to get a page of Ftps");
        Page<Ftp> page = ftpService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/ftps");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /ftps/:id : get the "id" ftp.
     *
     * @param id the id of the ftp to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ftp, or with status 404 (Not Found)
     */
    @GetMapping("/ftps/{id}")
    @Timed
    public ResponseEntity<Ftp> getFtp(@PathVariable Long id) {
        log.debug("REST request to get Ftp : {}", id);
        Ftp ftp = ftpService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ftp));
    }

    /**
     * DELETE  /ftps/:id : delete the "id" ftp.
     *
     * @param id the id of the ftp to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/ftps/{id}")
    @Timed
    public ResponseEntity<Void> deleteFtp(@PathVariable Long id) {
        log.debug("REST request to delete Ftp : {}", id);
        ftpService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
