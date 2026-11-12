package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.TypeDocumentService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.TypeDocumentDTO;
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
 * REST controller for managing TypeDocument.
 */
@RestController
@RequestMapping("/api")
public class TypeDocumentResource {

    private final Logger log = LoggerFactory.getLogger(TypeDocumentResource.class);

    private static final String ENTITY_NAME = "typeDocument";

    private final TypeDocumentService typeDocumentService;

    public TypeDocumentResource(TypeDocumentService typeDocumentService) {
        this.typeDocumentService = typeDocumentService;
    }

    /**
     * POST  /type-documents : Create a new typeDocument.
     *
     * @param typeDocumentDTO the typeDocumentDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new typeDocumentDTO, or with status 400 (Bad Request) if the typeDocument has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/type-documents")
    @Timed
    public ResponseEntity<TypeDocumentDTO> createTypeDocument(@Valid @RequestBody TypeDocumentDTO typeDocumentDTO) throws URISyntaxException {
        log.debug("REST request to save TypeDocument : {}", typeDocumentDTO);
        if (typeDocumentDTO.getId() != null) {
            throw new BadRequestAlertException("A new typeDocument cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TypeDocumentDTO result = typeDocumentService.save(typeDocumentDTO);
        return ResponseEntity.created(new URI("/api/type-documents/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /type-documents : Updates an existing typeDocument.
     *
     * @param typeDocumentDTO the typeDocumentDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated typeDocumentDTO,
     * or with status 400 (Bad Request) if the typeDocumentDTO is not valid,
     * or with status 500 (Internal Server Error) if the typeDocumentDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/type-documents")
    @Timed
    public ResponseEntity<TypeDocumentDTO> updateTypeDocument(@Valid @RequestBody TypeDocumentDTO typeDocumentDTO) throws URISyntaxException {
        log.debug("REST request to update TypeDocument : {}", typeDocumentDTO);
        if (typeDocumentDTO.getId() == null) {
            return createTypeDocument(typeDocumentDTO);
        }
        TypeDocumentDTO result = typeDocumentService.save(typeDocumentDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, typeDocumentDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /type-documents : get all the typeDocuments.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of typeDocuments in body
     */
    @GetMapping("/type-documents")
    @Timed
    public ResponseEntity<List<TypeDocumentDTO>> getAllTypeDocuments(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of TypeDocuments");
        Page<TypeDocumentDTO> page = typeDocumentService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/type-documents");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /type-documents/:id : get the "id" typeDocument.
     *
     * @param id the id of the typeDocumentDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the typeDocumentDTO, or with status 404 (Not Found)
     */
    @GetMapping("/type-documents/{id}")
    @Timed
    public ResponseEntity<TypeDocumentDTO> getTypeDocument(@PathVariable Long id) {
        log.debug("REST request to get TypeDocument : {}", id);
        TypeDocumentDTO typeDocumentDTO = typeDocumentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(typeDocumentDTO));
    }

    /**
     * DELETE  /type-documents/:id : delete the "id" typeDocument.
     *
     * @param id the id of the typeDocumentDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/type-documents/{id}")
    @Timed
    public ResponseEntity<Void> deleteTypeDocument(@PathVariable Long id) {
        log.debug("REST request to delete TypeDocument : {}", id);
        typeDocumentService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
