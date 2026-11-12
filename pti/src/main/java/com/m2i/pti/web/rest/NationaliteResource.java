package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Nationalite;
import com.m2i.pti.service.NationaliteService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.NationaliteDTO;
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
 * REST controller for managing Nationalite.
 */
@RestController
@RequestMapping("/api")
public class NationaliteResource {

    private final Logger log = LoggerFactory.getLogger(NationaliteResource.class);

    private static final String ENTITY_NAME = "nationalite";

    private final NationaliteService nationaliteService;

    public NationaliteResource(NationaliteService nationaliteService) {
        this.nationaliteService = nationaliteService;
    }

    /**
     * POST /nationalites : Create a new nationalite.
     *
     * @param nationaliteDTO the nationaliteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new nationaliteDTO, or with status 400 (Bad Request) if the nationalite
     * has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/nationalites")
    @Timed
    public ResponseEntity<NationaliteDTO> createNationalite(@RequestBody NationaliteDTO nationaliteDTO) throws URISyntaxException {
        log.debug("REST request to save Nationalite : {}", nationaliteDTO);
        if (nationaliteDTO.getId() != null) {
            throw new BadRequestAlertException("A new nationalite cannot already have an ID", ENTITY_NAME, "idexists");
        }
        NationaliteDTO result = nationaliteService.save(nationaliteDTO);
        return ResponseEntity.created(new URI("/api/nationalites/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT /nationalites : Updates an existing nationalite.
     *
     * @param nationaliteDTO the nationaliteDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated
     * nationaliteDTO, or with status 400 (Bad Request) if the nationaliteDTO is
     * not valid, or with status 500 (Internal Server Error) if the
     * nationaliteDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/nationalites")
    @Timed
    public ResponseEntity<NationaliteDTO> updateNationalite(@RequestBody NationaliteDTO nationaliteDTO) throws URISyntaxException {
        log.debug("REST request to update Nationalite : {}", nationaliteDTO);
        if (nationaliteDTO.getId() == null) {
            return createNationalite(nationaliteDTO);
        }
        NationaliteDTO result = nationaliteService.save(nationaliteDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, nationaliteDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET /nationalites : get all the nationalites.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of
     * nationalites in body
     */
    @GetMapping("/nationalites")
    @Timed
    public ResponseEntity<List<NationaliteDTO>> getAllNationalites(Pageable pageable) {
        log.debug("REST request to get a page of Nationalites");
        Page<NationaliteDTO> page = nationaliteService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/nationalites");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/listnationalites")
    @Timed
    public ResponseEntity<List<Nationalite>> getListNationalites() {
        log.debug("REST request to get a page of Nationalites");
        List<Nationalite> listNationalite = nationaliteService.findAllNationalite();
        return new ResponseEntity<>(listNationalite, null, HttpStatus.OK);
    }

    /**
     * GET /nationalites/:id : get the "id" nationalite.
     *
     * @param id the id of the nationaliteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the
     * nationaliteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/nationalites/{id}")
    @Timed
    public ResponseEntity<NationaliteDTO> getNationalite(@PathVariable Long id) {
        log.debug("REST request to get Nationalite : {}", id);
        NationaliteDTO nationaliteDTO = nationaliteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(nationaliteDTO));
    }

    /**
     * DELETE /nationalites/:id : delete the "id" nationalite.
     *
     * @param id the id of the nationaliteDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/nationalites/{id}")
    @Timed
    public ResponseEntity<Void> deleteNationalite(@PathVariable Long id) {
        log.debug("REST request to delete Nationalite : {}", id);
        nationaliteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
