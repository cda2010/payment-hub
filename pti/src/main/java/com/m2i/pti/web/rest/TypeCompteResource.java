package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.TypeCompteService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.TypeCompteDTO;
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
 * REST controller for managing TypeCompte.
 */
@RestController
@RequestMapping("/api")
public class TypeCompteResource {

    private final Logger log = LoggerFactory.getLogger(TypeCompteResource.class);

    private static final String ENTITY_NAME = "typeCompte";

    private final TypeCompteService typeCompteService;

    public TypeCompteResource(TypeCompteService typeCompteService) {
        this.typeCompteService = typeCompteService;
    }

    /**
     * POST  /type-comptes : Create a new typeCompte.
     *
     * @param typeCompteDTO the typeCompteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new typeCompteDTO, or with status 400 (Bad Request) if the typeCompte has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/type-comptes")
    @Timed
    public ResponseEntity<TypeCompteDTO> createTypeCompte(@Valid @RequestBody TypeCompteDTO typeCompteDTO) throws URISyntaxException {
        log.debug("REST request to save TypeCompte : {}", typeCompteDTO);
        if (typeCompteDTO.getId() != null) {
            throw new BadRequestAlertException("A new typeCompte cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TypeCompteDTO result = typeCompteService.save(typeCompteDTO);
        return ResponseEntity.created(new URI("/api/type-comptes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /type-comptes : Updates an existing typeCompte.
     *
     * @param typeCompteDTO the typeCompteDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated typeCompteDTO,
     * or with status 400 (Bad Request) if the typeCompteDTO is not valid,
     * or with status 500 (Internal Server Error) if the typeCompteDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/type-comptes")
    @Timed
    public ResponseEntity<TypeCompteDTO> updateTypeCompte(@Valid @RequestBody TypeCompteDTO typeCompteDTO) throws URISyntaxException {
        log.debug("REST request to update TypeCompte : {}", typeCompteDTO);
        if (typeCompteDTO.getId() == null) {
            return createTypeCompte(typeCompteDTO);
        }
        TypeCompteDTO result = typeCompteService.save(typeCompteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, typeCompteDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /type-comptes : get all the typeComptes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of typeComptes in body
     */
    @GetMapping("/type-comptes")
    @Timed
    public ResponseEntity<List<TypeCompteDTO>> getAllTypeComptes(Pageable pageable) {
        log.debug("REST request to get a page of TypeComptes");
        Page<TypeCompteDTO> page = typeCompteService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/type-comptes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /type-comptes/:id : get the "id" typeCompte.
     *
     * @param id the id of the typeCompteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the typeCompteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/type-comptes/{id}")
    @Timed
    public ResponseEntity<TypeCompteDTO> getTypeCompte(@PathVariable Long id) {
        log.debug("REST request to get TypeCompte : {}", id);
        TypeCompteDTO typeCompteDTO = typeCompteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(typeCompteDTO));
    }

    /**
     * DELETE  /type-comptes/:id : delete the "id" typeCompte.
     *
     * @param id the id of the typeCompteDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/type-comptes/{id}")
    @Timed
    public ResponseEntity<Void> deleteTypeCompte(@PathVariable Long id) {
        log.debug("REST request to delete TypeCompte : {}", id);
        typeCompteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
