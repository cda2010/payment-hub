package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.PointVenteService;
import com.m2i.pti.service.dto.PointVenteDTO;
import com.m2i.pti.service.dto.PointVenteUserDTO;
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

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing PointVente.
 */
@RestController
@RequestMapping("/api")
public class PointVenteResource {

    private final Logger log = LoggerFactory.getLogger(PointVenteResource.class);

    private static final String ENTITY_NAME = "pointVente";

    private final PointVenteService pointVenteService;

    public PointVenteResource(PointVenteService pointVenteService) {
        this.pointVenteService = pointVenteService;
    }

    /**
     * POST  /point-ventes : Create a new pointVente.
     *
     * @param pointVenteDTO the pointVenteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new pointVenteDTO, or with status 400 (Bad Request) if the pointVente has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/point-ventes")
    @Timed
    public ResponseEntity<PointVenteDTO> createPointVente(@Valid @RequestBody PointVenteDTO pointVenteDTO) throws URISyntaxException {
        log.debug("REST request to save PointVente : {}", pointVenteDTO);
        if (pointVenteDTO.getId() != null) {
            throw new BadRequestAlertException("A new pointVente cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PointVenteDTO result = pointVenteService.save(pointVenteDTO);
        return ResponseEntity.created(new URI("/api/point-ventes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /point-ventes : Updates an existing pointVente.
     *
     * @param pointVenteDTO the pointVenteDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated pointVenteDTO,
     * or with status 400 (Bad Request) if the pointVenteDTO is not valid,
     * or with status 500 (Internal Server Error) if the pointVenteDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/point-ventes")
    @Timed
    public ResponseEntity<PointVenteDTO> updatePointVente(@Valid @RequestBody PointVenteDTO pointVenteDTO) throws URISyntaxException {
        log.debug("REST request to update PointVente : {}", pointVenteDTO);
        if (pointVenteDTO.getId() == null) {
            return createPointVente(pointVenteDTO);
        }
        PointVenteDTO result = pointVenteService.save(pointVenteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, pointVenteDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /point-ventes : get all the pointVentes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of pointVentes in body
     */
    @GetMapping("/point-ventes")
    @Timed
    public ResponseEntity<List<PointVenteDTO>> getAllPointVentes(Pageable pageable) {
        log.debug("REST request to get a page of PointVentes");
        Page<PointVenteDTO> page = pointVenteService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/point-ventes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /point-ventes : get pointVentes BY UO.
     *
     * @param uoId information
     * @return the ResponseEntity with status 200 (OK) and the list of pointVentes in body
     */
    @GetMapping("/point-ventesByUO/{uoId}")
    @Timed
    public List<PointVenteDTO> getPointVenteByUO(@PathVariable Long uoId) {
        log.debug("REST request to get a page of PointVentes");
        List<PointVenteDTO> pointVenteDTOS = pointVenteService.findByUO(uoId);
        return pointVenteDTOS;
    }

    /**
     * GET  /point-ventes/:id : get the "id" pointVente.
     *
     * @param id the id of the pointVenteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the pointVenteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/point-ventes/{id}")
    @Timed
    public ResponseEntity<PointVenteDTO> getPointVente(@PathVariable Long id) {
        log.debug("REST request to get PointVente : {}", id);
        PointVenteDTO pointVenteDTO = pointVenteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(pointVenteDTO));
    }

    /**
     *
     *
     * @param uoId the id of the pointVenteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the pointVenteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/point-ventes/codePV")
    @Timed
    public ResponseEntity<PointVenteDTO> getCodePointVente(@RequestParam Long uoId) {
        log.debug("REST request to get PointVente : {}", uoId);
        PointVenteDTO pointVenteDTO = pointVenteService.getCodePointVente(uoId);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(pointVenteDTO));
    }


    /**
     * DELETE  /point-ventes/:id : delete the "id" pointVente.
     *
     * @param id the id of the pointVenteDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/point-ventes/{id}")
    @Timed
    public ResponseEntity<Void> deletePointVente(@PathVariable Long id) {
        log.debug("REST request to delete PointVente : {}", id);
        pointVenteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @PutMapping("/point-ventes/users")
    @Timed
    public ResponseEntity<PointVenteDTO> addPoitVentesUsers(@RequestBody PointVenteUserDTO pointVenteUserDTO) {
        PointVenteDTO pointVenteDTO = pointVenteService.gestionUserPointVente(pointVenteUserDTO);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(pointVenteDTO));
    }

}
