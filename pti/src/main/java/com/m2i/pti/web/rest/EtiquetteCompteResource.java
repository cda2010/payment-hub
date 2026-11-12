package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.EtiquetteCompte;
import com.m2i.pti.repository.EtiquetteCompteRepository;
import com.m2i.pti.service.EtiquetteCompteService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.EtiquetteCompteDTO;
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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing EtiquetteCompte.
 */
@RestController
@RequestMapping("/api")
public class EtiquetteCompteResource {

    private final Logger log = LoggerFactory.getLogger(EtiquetteCompteResource.class);

    private static final String ENTITY_NAME = "etiquetteCompte";

    private final EtiquetteCompteService etiquetteCompteService;

    private final EtiquetteCompteRepository etiquetteCompteRepository;

    public EtiquetteCompteResource(EtiquetteCompteService etiquetteCompteService, EtiquetteCompteRepository etiquetteCompteRepository) {
        this.etiquetteCompteService = etiquetteCompteService;
        this.etiquetteCompteRepository = etiquetteCompteRepository;
    }

    /**
     * POST  /etiquette-comptes : Create a new etiquetteCompte.
     *
     * @param etiquetteCompteDTO the etiquetteCompteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new etiquetteCompteDTO, or with status 400 (Bad Request) if the etiquetteCompte has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/etiquette-comptes")
    @Timed
    public ResponseEntity<EtiquetteCompteDTO> createEtiquetteCompte(@RequestBody EtiquetteCompteDTO etiquetteCompteDTO) throws URISyntaxException {
        log.debug("REST request to save EtiquetteCompte : {}", etiquetteCompteDTO);
        if (etiquetteCompteDTO.getId() != null) {
            throw new BadRequestAlertException("A new etiquetteCompte cannot already have an ID", ENTITY_NAME, "idexists");
        }
      etiquetteCompteDTO.setLibelle( etiquetteCompteDTO.getLibelle().toUpperCase()); 
        EtiquetteCompteDTO result = etiquetteCompteService.save(etiquetteCompteDTO);
        return ResponseEntity.created(new URI("/api/etiquette-comptes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /etiquette-comptes : Updates an existing etiquetteCompte.
     *
     * @param etiquetteCompteDTO the etiquetteCompteDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated etiquetteCompteDTO,
     * or with status 400 (Bad Request) if the etiquetteCompteDTO is not valid,
     * or with status 500 (Internal Server Error) if the etiquetteCompteDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/etiquette-comptes")
    @Timed
    public ResponseEntity<EtiquetteCompteDTO> updateEtiquetteCompte(@RequestBody EtiquetteCompteDTO etiquetteCompteDTO) throws URISyntaxException {
        log.debug("REST request to update EtiquetteCompte : {}", etiquetteCompteDTO);
        if (etiquetteCompteDTO.getId() == null) {
            return createEtiquetteCompte(etiquetteCompteDTO);
        }
        EtiquetteCompteDTO result = etiquetteCompteService.save(etiquetteCompteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, etiquetteCompteDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /etiquette-comptes : get all the etiquetteComptes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of etiquetteComptes in body
     */
    @GetMapping("/etiquette-comptes")
    @Timed
    public ResponseEntity<List<EtiquetteCompteDTO>> getAllEtiquetteComptes(Pageable pageable) {
        log.debug("REST request to get a page of EtiquetteComptes");
        Page<EtiquetteCompteDTO> page = etiquetteCompteService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/etiquette-comptes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /etiquette-comptes : get all the etiquetteComptes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of etiquetteComptes in body
     */
    @GetMapping("/etiquette-comptes_list")
    @Timed
    public List<EtiquetteCompteDTO> getAllEtiquetteComptes() {
        log.debug("REST request to get a page of EtiquetteComptes");
        List<EtiquetteCompteDTO> etiquetteCompteDTOS = etiquetteCompteService.findAllList();
        return etiquetteCompteDTOS;
    }

    /**
     * GET  /etiquette-comptes/:id : get the "id" etiquetteCompte.
     *
     * @param id the id of the etiquetteCompteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the etiquetteCompteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/etiquette-comptes/{id}")
    @Timed
    public ResponseEntity<EtiquetteCompteDTO> getEtiquetteCompte(@PathVariable Long id) {
        log.debug("REST request to get EtiquetteCompte : {}", id);
        EtiquetteCompteDTO etiquetteCompteDTO = etiquetteCompteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(etiquetteCompteDTO));
    }

    /**
     * DELETE  /etiquette-comptes/:id : delete the "id" etiquetteCompte.
     *
     * @param id the id of the etiquetteCompteDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/etiquette-comptes/{id}")
    @Timed
    public ResponseEntity<Void> deleteEtiquetteCompte(@PathVariable Long id) {
        log.debug("REST request to delete EtiquetteCompte : {}", id);
        etiquetteCompteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    @GetMapping("/etiquette-comptes-lib")
    @Timed
    public HashMap getEtiquetteName(@RequestParam(required = false) List<String> liste) {
        log.debug("--Request to get liste etiquette----"+liste);
        if(liste!=null) {
            return etiquetteCompteService.getListeLib(liste);
        }
        return null;
    }
    @GetMapping("/etiquettePTI")
    @Timed
    public List<EtiquetteCompte> getEtiquetteNamePTI() {
        return etiquetteCompteService.getEtiquetteNamePTI();

    }
}
