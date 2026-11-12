package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.HistExecutionJobService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.HistExecutionJobDTO;
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
 * REST controller for managing HistExecutionJob.
 */
@RestController
@RequestMapping("/api")
public class HistExecutionJobResource {

    private final Logger log = LoggerFactory.getLogger(HistExecutionJobResource.class);

    private static final String ENTITY_NAME = "histExecutionJob";

    private final HistExecutionJobService histExecutionJobService;

    public HistExecutionJobResource(HistExecutionJobService histExecutionJobService) {
        this.histExecutionJobService = histExecutionJobService;
    }

    /**
     * POST  /hist-execution-jobs : Create a new histExecutionJob.
     *
     * @param histExecutionJobDTO the histExecutionJobDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new histExecutionJobDTO, or with status 400 (Bad Request) if the histExecutionJob has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/hist-execution-jobs")
    @Timed
    public ResponseEntity<HistExecutionJobDTO> createHistExecutionJob(@RequestBody HistExecutionJobDTO histExecutionJobDTO) throws URISyntaxException {
        log.debug("REST request to save HistExecutionJob : {}", histExecutionJobDTO);
        if (histExecutionJobDTO.getId() != null) {
            throw new BadRequestAlertException("A new histExecutionJob cannot already have an ID", ENTITY_NAME, "idexists");
        }
        HistExecutionJobDTO result = histExecutionJobService.save(histExecutionJobDTO);
        return ResponseEntity.created(new URI("/api/hist-execution-jobs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /hist-execution-jobs : Updates an existing histExecutionJob.
     *
     * @param histExecutionJobDTO the histExecutionJobDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated histExecutionJobDTO,
     * or with status 400 (Bad Request) if the histExecutionJobDTO is not valid,
     * or with status 500 (Internal Server Error) if the histExecutionJobDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/hist-execution-jobs")
    @Timed
    public ResponseEntity<HistExecutionJobDTO> updateHistExecutionJob(@RequestBody HistExecutionJobDTO histExecutionJobDTO) throws URISyntaxException {
        log.debug("REST request to update HistExecutionJob : {}", histExecutionJobDTO);
        if (histExecutionJobDTO.getId() == null) {
            return createHistExecutionJob(histExecutionJobDTO);
        }
        HistExecutionJobDTO result = histExecutionJobService.save(histExecutionJobDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, histExecutionJobDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /hist-execution-jobs : get all the histExecutionJobs.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of histExecutionJobs in body
     */
    @GetMapping("/hist-execution-jobs")
    @Timed
    public ResponseEntity<List<HistExecutionJobDTO>> getAllHistExecutionJobs(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of HistExecutionJobs");
        Page<HistExecutionJobDTO> page = histExecutionJobService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/hist-execution-jobs");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/hist-execution-jobs/by-pti-job-details/{id}")
    @Timed
    public ResponseEntity<List<HistExecutionJobDTO>> getAllByPtiJobDetail(@PathVariable Long id, @ApiParam Pageable pageable) {
        log.debug("REST request to get a page of HistExecutionJobs");
        Page<HistExecutionJobDTO> page = histExecutionJobService.findAllByPtiJobDetail(id,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/hist-execution-jobs");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /hist-execution-jobs/:id : get the "id" histExecutionJob.
     *
     * @param id the id of the histExecutionJobDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the histExecutionJobDTO, or with status 404 (Not Found)
     */
    @GetMapping("/hist-execution-jobs/{id}")
    @Timed
    public ResponseEntity<HistExecutionJobDTO> getHistExecutionJob(@PathVariable Long id) {
        log.debug("REST request to get HistExecutionJob : {}", id);
        HistExecutionJobDTO histExecutionJobDTO = histExecutionJobService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(histExecutionJobDTO));
    }

    /**
     * DELETE  /hist-execution-jobs/:id : delete the "id" histExecutionJob.
     *
     * @param id the id of the histExecutionJobDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/hist-execution-jobs/{id}")
    @Timed
    public ResponseEntity<Void> deleteHistExecutionJob(@PathVariable Long id) {
        log.debug("REST request to delete HistExecutionJob : {}", id);
        histExecutionJobService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
