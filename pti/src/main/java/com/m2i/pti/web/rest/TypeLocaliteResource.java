package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.TypeLocaliteService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.TypeLocaliteDTO;
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
 * REST controller for managing TypeLocalite.
 */
@RestController
@RequestMapping("/api")
public class TypeLocaliteResource {

    private final Logger log = LoggerFactory.getLogger(TypeLocaliteResource.class);

    private static final String ENTITY_NAME = "typeLocalite";

    private final TypeLocaliteService typeLocaliteService;

    public TypeLocaliteResource(TypeLocaliteService typeLocaliteService) {
        this.typeLocaliteService = typeLocaliteService;
    }

    /**
     * POST  /type-localites : Create a new typeLocalite.
     *
     * @param typeLocaliteDTO the typeLocaliteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new typeLocaliteDTO, or with status 400 (Bad Request) if the typeLocalite has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/type-localites")
    @Timed
    public ResponseEntity<TypeLocaliteDTO> createTypeLocalite(@RequestBody TypeLocaliteDTO typeLocaliteDTO) throws URISyntaxException {
        log.debug("REST request to save TypeLocalite : {}", typeLocaliteDTO);
        if (typeLocaliteDTO.getId() != null) {
            throw new BadRequestAlertException("A new typeLocalite cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TypeLocaliteDTO result = typeLocaliteService.save(typeLocaliteDTO);
        return ResponseEntity.created(new URI("/api/type-localites/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /type-localites : Updates an existing typeLocalite.
     *
     * @param typeLocaliteDTO the typeLocaliteDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated typeLocaliteDTO,
     * or with status 400 (Bad Request) if the typeLocaliteDTO is not valid,
     * or with status 500 (Internal Server Error) if the typeLocaliteDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/type-localites")
    @Timed
    public ResponseEntity<TypeLocaliteDTO> updateTypeLocalite(@RequestBody TypeLocaliteDTO typeLocaliteDTO) throws URISyntaxException {
        log.debug("REST request to update TypeLocalite : {}", typeLocaliteDTO);
        if (typeLocaliteDTO.getId() == null) {
            return createTypeLocalite(typeLocaliteDTO);
        }
        TypeLocaliteDTO result = typeLocaliteService.save(typeLocaliteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, typeLocaliteDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /type-localites : get all the typeLocalites.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of typeLocalites in body
     */
    @GetMapping("/type-localites")
    @Timed
    public ResponseEntity<List<TypeLocaliteDTO>> getAllTypeLocalites(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of TypeLocalites");
        Page<TypeLocaliteDTO> page = typeLocaliteService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/type-localites");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /type-localites/:id : get the "id" typeLocalite.
     *
     * @param id the id of the typeLocaliteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the typeLocaliteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/type-localites/{id}")
    @Timed
    public ResponseEntity<TypeLocaliteDTO> getTypeLocalite(@PathVariable Long id) {
        log.debug("REST request to get TypeLocalite : {}", id);
        TypeLocaliteDTO typeLocaliteDTO = typeLocaliteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(typeLocaliteDTO));
    }

    /**
     * DELETE  /type-localites/:id : delete the "id" typeLocalite.
     *
     * @param id the id of the typeLocaliteDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/type-localites/{id}")
    @Timed
    public ResponseEntity<Void> deleteTypeLocalite(@PathVariable Long id) {
        log.debug("REST request to delete TypeLocalite : {}", id);
        typeLocaliteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
