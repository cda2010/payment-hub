package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.ParamExecutionJob;
import com.m2i.pti.repository.ParamExecutionJobRepository;
import com.m2i.pti.service.ParamExecutionJobService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing ParamExecutionJob.
 */
@RestController
@RequestMapping("/api")
public class ParamExecutionJobResource {

    private final Logger log = LoggerFactory.getLogger(ParamExecutionJobResource.class);

    private static final String ENTITY_NAME = "paramExecutionJob";

    private final ParamExecutionJobService paramExecutionJobService;

    private final ParamExecutionJobRepository paramExecutionJobRepository;

    public ParamExecutionJobResource(ParamExecutionJobService paramExecutionJobService, ParamExecutionJobRepository paramExecutionJobRepository) {
        this.paramExecutionJobService = paramExecutionJobService;
        this.paramExecutionJobRepository = paramExecutionJobRepository;
    }

    /**
     * POST  /param-execution-jobs : Create a new paramExecutionJob.
     *
     * @param paramExecutionJob the paramExecutionJob to create
     * @return the ResponseEntity with status 201 (Created) and with body the new paramExecutionJob, or with status 400 (Bad Request) if the paramExecutionJob has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/param-execution-jobs")
    @Timed
    public ResponseEntity<ParamExecutionJob> createParamExecutionJob(@RequestBody ParamExecutionJob paramExecutionJob) throws URISyntaxException {
        log.debug("REST request to save ParamExecutionJob : {}", paramExecutionJob);
        if (paramExecutionJob.getId() != null) {
            throw new BadRequestAlertException("A new paramExecutionJob cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ParamExecutionJob result = paramExecutionJobService.save(paramExecutionJob);
        return ResponseEntity.created(new URI("/api/param-execution-jobs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /param-execution-jobs : Updates an existing paramExecutionJob.
     *
     * @param paramExecutionJob the paramExecutionJob to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated paramExecutionJob,
     * or with status 400 (Bad Request) if the paramExecutionJob is not valid,
     * or with status 500 (Internal Server Error) if the paramExecutionJob couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/param-execution-jobs")
    @Timed
    public ResponseEntity<ParamExecutionJob> updateParamExecutionJob(@RequestBody ParamExecutionJob paramExecutionJob) throws URISyntaxException {
        log.debug("REST request to update ParamExecutionJob : {}", paramExecutionJob);
        if (paramExecutionJob.getId() == null) {
            return createParamExecutionJob(paramExecutionJob);
        }
        ParamExecutionJob result = paramExecutionJobService.save(paramExecutionJob);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, paramExecutionJob.getId().toString()))
            .body(result);
    }

    /**
     * GET  /param-execution-jobs : get all the paramExecutionJobs.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of paramExecutionJobs in body
     */
    @GetMapping("/param-execution-jobs")
    @Timed
    public List<ParamExecutionJob> getAllParamExecutionJobs() {
        log.debug("REST request to get all ParamExecutionJobs");
        return paramExecutionJobService.findAll();
        }

    /**
     * GET  /param-execution-jobs/:id : get the "id" paramExecutionJob.
     *
     * @param id the id of the paramExecutionJob to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the paramExecutionJob, or with status 404 (Not Found)
     */
    @GetMapping("/param-execution-jobs/{id}")
    @Timed
    public ResponseEntity<ParamExecutionJob> getParamExecutionJob(@PathVariable Long id) {
        log.debug("REST request to get ParamExecutionJob : {}", id);
        ParamExecutionJob paramExecutionJob = paramExecutionJobService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(paramExecutionJob));
    }

    /**
     * DELETE  /param-execution-jobs/:id : delete the "id" paramExecutionJob.
     *
     * @param id the id of the paramExecutionJob to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/param-execution-jobs/{id}")
    @Timed
    public ResponseEntity<Void> deleteParamExecutionJob(@PathVariable Long id) {
        log.debug("REST request to delete ParamExecutionJob : {}", id);
        paramExecutionJobService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/queryByPtiJobDetail")
    @Timed
    public List<ParamExecutionJob> queryByPtiJobDetail(@RequestParam Long id) {
        return paramExecutionJobRepository.queryByPtiJobDetail(id);
    }

    @GetMapping("/countByKeys")
    @Timed
    public HashMap countByKeys(@RequestParam Long cle){
        Integer nbr=paramExecutionJobRepository.countByKey(cle);
        Map<String, Integer> nombreKey = new HashMap<>();
        nombreKey.put("cle",nbr);
        return (HashMap) nombreKey;
    }
}
