package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.LogOperation;

import com.m2i.pti.repository.LogOperationRepository;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing LogOperation.
 */
@RestController
@RequestMapping("/api")
public class LogOperationResource {

    private final Logger log = LoggerFactory.getLogger(LogOperationResource.class);

    private static final String ENTITY_NAME = "logOperation";

    private final LogOperationRepository logOperationRepository;

    public LogOperationResource(LogOperationRepository logOperationRepository) {
        this.logOperationRepository = logOperationRepository;
    }

    /**
     * POST  /log-operations : Create a new logOperation.
     *
     * @param logOperation the logOperation to create
     * @return the ResponseEntity with status 201 (Created) and with body the new logOperation, or with status 400 (Bad Request) if the logOperation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/log-operations")
    @Timed
    public ResponseEntity<LogOperation> createLogOperation(@RequestBody LogOperation logOperation) throws URISyntaxException {
        log.debug("REST request to save LogOperation : {}", logOperation);
        if (logOperation.getId() != null) {
            throw new BadRequestAlertException("A new logOperation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        LogOperation result = logOperationRepository.save(logOperation);
        return ResponseEntity.created(new URI("/api/log-operations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /log-operations : Updates an existing logOperation.
     *
     * @param logOperation the logOperation to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated logOperation,
     * or with status 400 (Bad Request) if the logOperation is not valid,
     * or with status 500 (Internal Server Error) if the logOperation couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/log-operations")
    @Timed
    public ResponseEntity<LogOperation> updateLogOperation(@RequestBody LogOperation logOperation) throws URISyntaxException {
        log.debug("REST request to update LogOperation : {}", logOperation);
        if (logOperation.getId() == null) {
            return createLogOperation(logOperation);
        }
        LogOperation result = logOperationRepository.save(logOperation);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, logOperation.getId().toString()))
            .body(result);
    }

    /**
     * GET  /log-operations : get all the logOperations.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of logOperations in body
     */
    @GetMapping("/log-operations")
    @Timed
    public ResponseEntity<List<LogOperation>> getAllLogOperations(@RequestParam(required = false) String codeOperation, @RequestParam(required = false) String login,@RequestParam(required = false) ZonedDateTime debut,@RequestParam(required = false) ZonedDateTime fin, Pageable pageable) {
        log.debug("REST request to get all LogOperations");
        Page<LogOperation> page=null;
        if(login!=null&&codeOperation!=null){
            page=logOperationRepository.findByCodeOperationContainingAndLoginContaining(codeOperation,login,pageable);
        }else {
            if(login!=null){
                page=logOperationRepository.findByLoginContaining(login,pageable);
            }else {
                if (codeOperation!=null){
                    page=logOperationRepository.findByCodeOperationContaining(codeOperation,pageable);
                }else {
                    page=logOperationRepository.findAll(pageable);
                }
            }
        }
//        page=logOperationRepository.findByCriteria(codeOperation,login,debut,fin,pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/log-operations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        }

    /**
     * GET  /log-operations/:id : get the "id" logOperation.
     *
     * @param id the id of the logOperation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the logOperation, or with status 404 (Not Found)
     */
    @GetMapping("/log-operations/{id}")
    @Timed
    public ResponseEntity<LogOperation> getLogOperation(@PathVariable Long id) {
        log.debug("REST request to get LogOperation : {}", id);
        LogOperation logOperation = logOperationRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(logOperation));
    }

    /**
     * DELETE  /log-operations/:id : delete the "id" logOperation.
     *
     * @param id the id of the logOperation to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/log-operations/{id}")
    @Timed
    public ResponseEntity<Void> deleteLogOperation(@PathVariable Long id) {
        log.debug("REST request to delete LogOperation : {}", id);
        logOperationRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
