package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2i.pti.domain.LigneReglement;
import com.m2i.pti.domain.LigneReglementTemplate;
import com.m2i.pti.domain.Reglement;
import com.m2i.pti.domain.ReglementTemplate;
import com.m2i.pti.domain.User;
import com.m2i.pti.domain.util.JsonLigneReglementTemplate;

import com.m2i.pti.service.LigneReglementDTO;
import com.m2i.pti.service.ReglementService;
import com.m2i.pti.service.ReglementTemplateService;
import com.m2i.pti.service.UserService;
import com.m2i.pti.service.dto.ReglementDTO;
import com.m2i.pti.service.dto.UoDTO;
import com.m2i.pti.service.mapper.LigneReglementMapper;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
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
import java.util.ArrayList;
import java.util.HashSet;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * REST controller for managing Reglement.
 */
@RestController
@RequestMapping("/api")
public class ReglementResource {

    private final Logger log = LoggerFactory.getLogger(ReglementResource.class);

    private static final String ENTITY_NAME = "reglement";

    private final LigneReglementMapper ligneReglementMapper;
    ReglementTemplateService reglementTemplateService;
    private final ReglementService reglementService;
    UserService userService;

    public ReglementResource(UserService userService, LigneReglementMapper ligneReglementMapper, ReglementService reglementService, ReglementTemplateService reglementTemplateService) {
        this.ligneReglementMapper = ligneReglementMapper;
        this.reglementService = reglementService;
        this.reglementTemplateService = reglementTemplateService;
        this.userService = userService;
    }

    /**
     * POST /reglements : Create a new reglement.
     *
     * @param reglement the reglement to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new reglement, or with status 400 (Bad Request) if the reglement has
     * already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/reglements")
    @Timed
    public ResponseEntity<Reglement> createReglement(@RequestBody Reglement reglement) throws URISyntaxException {
        log.debug("REST request to save Reglement : {}", reglement);
        if (reglement.getId() != null) {
            throw new BadRequestAlertException("A new reglement cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Reglement result = reglementService.save(reglement);
        return ResponseEntity.created(new URI("/api/reglements/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @DeleteMapping("/reglements/reglementTemplate/{id}")
    @Timed
    public ResponseEntity<Void> deleteReglementTemplate(@PathVariable Long id) {
        log.debug("REST request to delete Reglement : {}", id);
        reglementTemplateService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @PostMapping("/reglements/reglementTemplate")
    @Timed
    public ResponseEntity<ReglementTemplate> createReglementTemplate(@RequestBody ReglementTemplate reglementTemplate) throws URISyntaxException, JsonProcessingException {
        log.debug("REST request to save Reglement : {}", reglementTemplate);
     

            User userConnecte = userService.getUserConnecteOptimise(reglementTemplate.getLogin());
        if (userConnecte !=null &&  userConnecte.getPointVente()!=null  && userConnecte.getPointVente().getId()==null ){
                   throw new CustomParameterizedException("Impossible de créer le template : Pas de point de vente .");
        }
        if (reglementTemplate.getId() != null) {
            throw new BadRequestAlertException("A new reglement cannot already have an ID", ENTITY_NAME, "idexists");
        }

        ReglementTemplate result = reglementTemplateService.save(reglementTemplate);
        return ResponseEntity.created(new URI("/api/reglements/reglementTemplate" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT /reglements : Updates an existing reglement.
     *
     * @param reglement the reglement to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated
     * reglement, or with status 400 (Bad Request) if the reglement is not
     * valid, or with status 500 (Internal Server Error) if the reglement
     * couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/reglements")
    @Timed
    public ResponseEntity<Reglement> updateReglement(@RequestBody Reglement reglement) throws URISyntaxException {
        log.debug("REST request to update Reglement : {}", reglement);
        if (reglement.getId() == null) {
            return createReglement(reglement);
        }
        Reglement result = reglementService.save(reglement);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, reglement.getId().toString()))
                .body(result);
    }

    /**
     * GET /reglements : get all the reglements.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of
     * reglements in body
     */
    @GetMapping("/reglements")
    @Timed
    public ResponseEntity<List<ReglementDTO>> getAllReglements(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of Reglements");
        Page<ReglementDTO> page = reglementService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/reglements");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/reglements/reglementTemplate")
    @Timed
    public ResponseEntity<List<ReglementTemplate>> getAllReglementsTemplate(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of Reglements");
        Page<ReglementTemplate> page = reglementTemplateService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/reglements");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/reglements/reglementTemplate/all")
    @Timed
    public List<ReglementTemplate> getAllReglementsTemplate() {
        log.debug("REST request to get a page of Reglements");
        List<ReglementTemplate> listRegle = reglementTemplateService.findAll();

        return listRegle;
    }

    /**
     * GET /reglements/:id : get the "id" reglement.
     *
     * @param id the id of the reglement to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the
     * reglement, or with status 404 (Not Found)
     */
    @GetMapping("/reglements/{id}")
    @Timed
    public ResponseEntity<Reglement> getReglement(@PathVariable Long id) {
        log.debug("REST request to get Reglement : {}", id);
        Reglement reglement = reglementService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(reglement));
    }

    /**
     * DELETE /reglements/:id : delete the "id" reglement.
     *
     * @param id the id of the reglement to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/reglements/{id}")
    @Timed
    public ResponseEntity<Void> deleteReglement(@PathVariable Long id) {
        log.debug("REST request to delete Reglement : {}", id);
        reglementService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("getligneReglement")
    @Timed
    public ResponseEntity<List<LigneReglementDTO>> getLigneRelement(@RequestParam Long reglementId, Pageable pageable) {
        log.debug("-------Ligne reglement--------------" + reglementId);
        Page<LigneReglementDTO> page = reglementService.findLigne(reglementId, pageable).map(ligneReglementMapper::toDto);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/reglement-organisation");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
