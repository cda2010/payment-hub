package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.PtiJobDetails;
import com.m2i.pti.scheduler.service.SchedulerService;
import com.m2i.pti.service.PtiJobDetailsService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.PtiJobDetailsDTO;
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
import org.quartz.SchedulerException;

/**
 * REST controller for managing PtiJobDetails.
 */
@RestController
@RequestMapping("/api")
public class PtiJobDetailsResource {

    private final Logger log = LoggerFactory.getLogger(PtiJobDetailsResource.class);
    SchedulerService schedulerService;
    private static final String ENTITY_NAME = "ptiJobDetails";

    private final PtiJobDetailsService ptiJobDetailsService;

    public PtiJobDetailsResource(PtiJobDetailsService ptiJobDetailsService, SchedulerService schedulerService) {
        this.ptiJobDetailsService = ptiJobDetailsService;
        this.schedulerService = schedulerService;
    }

    /**
     * POST /pti-job-details : Create a new ptiJobDetails.
     *
     * @param ptiJobDetailsDTO the ptiJobDetailsDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new ptiJobDetailsDTO, or with status 400 (Bad Request) if the
     * ptiJobDetails has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/pti-job-details")
    @Timed
    public ResponseEntity<PtiJobDetailsDTO> createPtiJobDetails(@RequestBody PtiJobDetailsDTO ptiJobDetailsDTO) throws URISyntaxException, SchedulerException {
        log.debug("REST request to save PtiJobDetails : {}", ptiJobDetailsDTO);
        if (ptiJobDetailsDTO.getId() != null) {
            throw new BadRequestAlertException("A new ptiJobDetails cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PtiJobDetailsDTO result = ptiJobDetailsService.save(ptiJobDetailsDTO);
        if (result != null) {
            PtiJobDetails ptiJobDetails = ptiJobDetailsService.toEntityMap(ptiJobDetailsDTO);
            schedulerService.scheduleNewJob(ptiJobDetails);
        }
        return ResponseEntity.created(new URI("/api/pti-job-details/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT /pti-job-details : Updates an existing ptiJobDetails.
     *
     * @param ptiJobDetailsDTO the ptiJobDetailsDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated
     * ptiJobDetailsDTO, or with status 400 (Bad Request) if the
     * ptiJobDetailsDTO is not valid, or with status 500 (Internal Server Error)
     * if the ptiJobDetailsDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/pti-job-details")
    @Timed
    public ResponseEntity<PtiJobDetailsDTO> updatePtiJobDetails(@RequestBody PtiJobDetailsDTO ptiJobDetailsDTO) throws URISyntaxException, SchedulerException {
        log.debug("REST request to update PtiJobDetails : {}", ptiJobDetailsDTO);
        if (ptiJobDetailsDTO.getId() == null) {
            return createPtiJobDetails(ptiJobDetailsDTO);
        }
        PtiJobDetailsDTO result = ptiJobDetailsService.save(ptiJobDetailsDTO);
        if (result != null) {
            PtiJobDetails ptiJobDetails = ptiJobDetailsService.toEntityMap(ptiJobDetailsDTO);
            schedulerService.updateScheduleJob(ptiJobDetails);
        }
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ptiJobDetailsDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET /pti-job-details : get all the ptiJobDetails.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of
     * ptiJobDetails in body
     */
    @GetMapping("/pti-job-details")
    @Timed
    public ResponseEntity<List<PtiJobDetailsDTO>> getAllPtiJobDetails(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of PtiJobDetails");

        Page<PtiJobDetailsDTO> page = ptiJobDetailsService.findAll(pageable);
        log.debug("{}", page);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/pti-job-details");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET /pti-job-details/:id : get the "id" ptiJobDetails.
     *
     * @param id the id of the ptiJobDetailsDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the
     * ptiJobDetailsDTO, or with status 404 (Not Found)
     */
    @GetMapping("/pti-job-details/{id}")
    @Timed
    public ResponseEntity<PtiJobDetailsDTO> getPtiJobDetails(@PathVariable Long id) {
        log.debug("REST request to get PtiJobDetails : {}", id);
        PtiJobDetailsDTO ptiJobDetailsDTO = ptiJobDetailsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ptiJobDetailsDTO));
    }

    @GetMapping("/pti-job-details/start/{id}")
    @Timed
    public ResponseEntity<PtiJobDetails> startPtiJobDetails(@PathVariable Long id) {
        log.debug("REST request to get PtiJobDetails : {}", id);
        PtiJobDetailsDTO ptiJobDetailsDTO = ptiJobDetailsService.findOne(id);
        PtiJobDetails ptiJobDetails = ptiJobDetailsService.toEntityMap(ptiJobDetailsDTO);
        schedulerService.startJobNow(ptiJobDetails.getJobName(), ptiJobDetails.getNomGroupe());
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ptiJobDetails));
    }

    @GetMapping("/pti-job-details/pause/{id}")
    @Timed
    public ResponseEntity<PtiJobDetails> pausePtiJobDetails(@PathVariable Long id) {
        log.debug("REST request to get PtiJobDetails : {}", id);
        PtiJobDetailsDTO ptiJobDetailsDTO = ptiJobDetailsService.findOne(id);
        PtiJobDetails ptiJobDetails = ptiJobDetailsService.toEntityMap(ptiJobDetailsDTO);
        schedulerService.pauseJob(ptiJobDetails.getJobName(), ptiJobDetails.getNomGroupe());
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ptiJobDetails));
    }

    @GetMapping("/pti-job-details/unschedle/{id}")
    @Timed
    public ResponseEntity<PtiJobDetails> unschedlePtiJobDetails(@PathVariable Long id) {
        log.debug("REST request to get PtiJobDetails : {}", id);
        PtiJobDetailsDTO ptiJobDetailsDTO = ptiJobDetailsService.findOne(id);
        PtiJobDetails ptiJobDetails = ptiJobDetailsService.toEntityMap(ptiJobDetailsDTO);
        schedulerService.unScheduleJob(ptiJobDetails.getJobName());

        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ptiJobDetails));
    }

    @GetMapping("/pti-job-details/restart/{id}")
    @Timed
    public ResponseEntity<PtiJobDetails> retartPtiJobDetails(@PathVariable Long id) {
        log.debug("REST request to get PtiJobDetails : {}", id);
        PtiJobDetailsDTO ptiJobDetailsDTO = ptiJobDetailsService.findOne(id);
        PtiJobDetails ptiJobDetails = ptiJobDetailsService.toEntityMap(ptiJobDetailsDTO);
        schedulerService.resumeJob(ptiJobDetails.getJobName(), ptiJobDetails.getNomGroupe());

        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ptiJobDetails));
    }

    /**
     * DELETE /pti-job-details/:id : delete the "id" ptiJobDetails.
     *
     * @param id the id of the ptiJobDetailsDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/pti-job-details/{id}")
    @Timed
    public ResponseEntity<Void> deletePtiJobDetails(@PathVariable Long id) throws SchedulerException {
        log.debug("REST request to delete PtiJobDetails : {}", id);
        PtiJobDetailsDTO ptiJobDetailsDTO = ptiJobDetailsService.findOne(id);
        ptiJobDetailsService.delete(id);
        PtiJobDetails ptiJobDetails = ptiJobDetailsService.toEntityMap(ptiJobDetailsDTO);
        schedulerService.deleteJob(ptiJobDetails.getJobName(), ptiJobDetails.getNomGroupe());

        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }


}
