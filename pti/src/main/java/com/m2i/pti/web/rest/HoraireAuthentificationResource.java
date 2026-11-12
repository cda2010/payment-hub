package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.HoraireAuthentification;
import com.m2i.pti.service.HoraireAuthentificationService;
import com.m2i.pti.service.dto.TableauFraisDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.HoraireAuthentificationDTO;
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

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing HoraireAuthentification.
 */
@RestController
@RequestMapping("/api")
public class HoraireAuthentificationResource {

    private final Logger log = LoggerFactory.getLogger(HoraireAuthentificationResource.class);

    private static final String ENTITY_NAME = "horaireAuthentification";

    private final HoraireAuthentificationService horaireAuthentificationService;

    public HoraireAuthentificationResource(HoraireAuthentificationService horaireAuthentificationService) {
        this.horaireAuthentificationService = horaireAuthentificationService;
    }

    /**
     * POST  /horaire-authentifications : Create a new horaireAuthentification.
     *
     * @param horaireAuthentificationDTO the horaireAuthentificationDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new horaireAuthentificationDTO, or with status 400 (Bad Request) if the horaireAuthentification has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/horaire-authentifications")
    @Timed
    public ResponseEntity<HoraireAuthentificationDTO> createHoraireAuthentification(@Valid @RequestBody HoraireAuthentificationDTO horaireAuthentificationDTO) throws URISyntaxException {
        log.debug("REST request to save HoraireAuthentification : {}", horaireAuthentificationDTO);
        if (horaireAuthentificationDTO.getId() != null) {
            throw new BadRequestAlertException("A new horaireAuthentification cannot already have an ID", ENTITY_NAME, "idexists");
        }
        HoraireAuthentificationDTO result = horaireAuthentificationService.save(horaireAuthentificationDTO);
        return ResponseEntity.created(new URI("/api/horaire-authentifications/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    @GetMapping("/horaire-authentifications/all")
    public List<HoraireAuthentificationDTO> findAll(){
        return horaireAuthentificationService.findAll();
    }


    /**
     * PUT  /horaire-authentifications : Updates an existing horaireAuthentification.
     *
     * @param horaireAuthentificationDTO the horaireAuthentificationDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated horaireAuthentificationDTO,
     * or with status 400 (Bad Request) if the horaireAuthentificationDTO is not valid,
     * or with status 500 (Internal Server Error) if the horaireAuthentificationDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/horaire-authentifications")
    @Timed
    public ResponseEntity<HoraireAuthentificationDTO> updateHoraireAuthentification(@Valid @RequestBody HoraireAuthentificationDTO horaireAuthentificationDTO) throws URISyntaxException {
        log.debug("REST request to update HoraireAuthentification : {}", horaireAuthentificationDTO);
        if (horaireAuthentificationDTO.getId() == null) {
            return createHoraireAuthentification(horaireAuthentificationDTO);
        }
        HoraireAuthentificationDTO result = horaireAuthentificationService.save(horaireAuthentificationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, horaireAuthentificationDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /horaire-authentifications : get all the horaireAuthentifications.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of horaireAuthentifications in body
     */
    @GetMapping("/horaire-authentifications")
    @Timed
    public ResponseEntity<List<HoraireAuthentificationDTO>> getAllHoraireAuthentifications(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of HoraireAuthentifications");
        Page<HoraireAuthentificationDTO> page = horaireAuthentificationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/horaire-authentifications");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /horaire-authentifications/:id : get the "id" horaireAuthentification.
     *
     * @param id the id of the horaireAuthentificationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the horaireAuthentificationDTO, or with status 404 (Not Found)
     */
    @GetMapping("/horaire-authentifications/{id}")
    @Timed
    public ResponseEntity<HoraireAuthentificationDTO> getHoraireAuthentification(@PathVariable Long id) {
        log.debug("REST request to get HoraireAuthentification : {}", id);
        HoraireAuthentificationDTO horaireAuthentificationDTO = horaireAuthentificationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(horaireAuthentificationDTO));
    }

    /**
     * DELETE  /horaire-authentifications/:id : delete the "id" horaireAuthentification.
     *
     * @param id the id of the horaireAuthentificationDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/horaire-authentifications/{id}")
    @Timed
    public ResponseEntity<Void> deleteHoraireAuthentification(@PathVariable Long id) {
        log.debug("REST request to delete HoraireAuthentification : {}", id);
        horaireAuthentificationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
