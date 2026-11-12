package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.RecepteurAlarmeList;
import com.m2i.pti.service.RecepteurAlarmeService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.RecepteurAlarmeDTO;
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
 * REST controller for managing RecepteurAlarme.
 */
@RestController
@RequestMapping("/api")
public class RecepteurAlarmeResource {

    private final Logger log = LoggerFactory.getLogger(RecepteurAlarmeResource.class);

    private static final String ENTITY_NAME = "recepteurAlarme";

    private final RecepteurAlarmeService recepteurAlarmeService;

    public RecepteurAlarmeResource(RecepteurAlarmeService recepteurAlarmeService) {
        this.recepteurAlarmeService = recepteurAlarmeService;
    }

    /**
     * POST  /recepteur-alarmes : Create a new recepteurAlarme.
     *
     * @param recepteurAlarmeDTO the recepteurAlarmeDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new recepteurAlarmeDTO, or with status 400 (Bad Request) if the recepteurAlarme has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/recepteur-alarmes")
    @Timed
    public ResponseEntity<RecepteurAlarmeDTO> createRecepteurAlarme(@RequestBody RecepteurAlarmeDTO recepteurAlarmeDTO) throws URISyntaxException {
        log.debug("REST request to save RecepteurAlarme : {}", recepteurAlarmeDTO);
        if (recepteurAlarmeDTO.getId() != null) {
            throw new BadRequestAlertException("A new recepteurAlarme cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RecepteurAlarmeDTO result = recepteurAlarmeService.save(recepteurAlarmeDTO);
        return ResponseEntity.created(new URI("/api/recepteur-alarmes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /recepteur-alarmes : Updates an existing recepteurAlarme.
     *
     * @param recepteurAlarmeDTO the recepteurAlarmeDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated recepteurAlarmeDTO,
     * or with status 400 (Bad Request) if the recepteurAlarmeDTO is not valid,
     * or with status 500 (Internal Server Error) if the recepteurAlarmeDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/recepteur-alarmes")
    @Timed
    public ResponseEntity<RecepteurAlarmeDTO> updateRecepteurAlarme(@RequestBody RecepteurAlarmeDTO recepteurAlarmeDTO) throws URISyntaxException {
        log.debug("REST request to update RecepteurAlarme : {}", recepteurAlarmeDTO);
        if (recepteurAlarmeDTO.getId() == null) {
            return createRecepteurAlarme(recepteurAlarmeDTO);
        }
        RecepteurAlarmeDTO result = recepteurAlarmeService.save(recepteurAlarmeDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, recepteurAlarmeDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /recepteur-alarmes : get all the recepteurAlarmes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of recepteurAlarmes in body
     */
    @GetMapping("/recepteur-alarmes")
    @Timed
    public ResponseEntity<List<RecepteurAlarmeDTO>> getAllRecepteurAlarmes(@RequestParam(required = false) String etiquette,@RequestParam(required = false) String sms ,Pageable pageable) {
        log.debug("REST request to get a page of RecepteurAlarmes");
        Page<RecepteurAlarmeDTO> page = recepteurAlarmeService.findAll(etiquette,sms,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/recepteur-alarmes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    @GetMapping("/recepteur-alarmes/list")
    @Timed
    public ResponseEntity<RecepteurAlarmeList> getListRecepteurAlarmes(@RequestParam String etiquette) {
        RecepteurAlarmeList recepteurAlarmeList = recepteurAlarmeService.findListByEtiquette(etiquette);
        return new ResponseEntity<RecepteurAlarmeList>(recepteurAlarmeList,recepteurAlarmeList==null ? HttpStatus.NOT_FOUND:HttpStatus.FOUND);
    }

    /**
     * GET  /recepteur-alarmes/:id : get the "id" recepteurAlarme.
     *
     * @param id the id of the recepteurAlarmeDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the recepteurAlarmeDTO, or with status 404 (Not Found)
     */
    @GetMapping("/recepteur-alarmes/{id}")
    @Timed
    public ResponseEntity<RecepteurAlarmeDTO> getRecepteurAlarme(@PathVariable Long id) {
        log.debug("REST request to get RecepteurAlarme : {}", id);
        RecepteurAlarmeDTO recepteurAlarmeDTO = recepteurAlarmeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(recepteurAlarmeDTO));
    }

    /**
     * DELETE  /recepteur-alarmes/:id : delete the "id" recepteurAlarme.
     *
     * @param id the id of the recepteurAlarmeDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/recepteur-alarmes/{id}")
    @Timed
    public ResponseEntity<Void> deleteRecepteurAlarme(@PathVariable Long id) {
        log.debug("REST request to delete RecepteurAlarme : {}", id);
        recepteurAlarmeService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
