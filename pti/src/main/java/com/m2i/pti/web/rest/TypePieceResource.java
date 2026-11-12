package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.repository.TypePieceRepository;
import com.m2i.pti.service.TypePieceService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.TypePieceDTO;
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
 * REST controller for managing TypePiece.
 */
@RestController
@RequestMapping("/api")
public class TypePieceResource {

    private final Logger log = LoggerFactory.getLogger(TypePieceResource.class);

    private static final String ENTITY_NAME = "typePiece";

    private final TypePieceService typePieceService;


    public TypePieceResource(TypePieceService typePieceService) {
        this.typePieceService = typePieceService;
    }

    /**
     * POST  /type-pieces : Create a new typePiece.
     *
     * @param typePieceDTO the typePieceDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new typePieceDTO, or with status 400 (Bad Request) if the typePiece has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/type-pieces")
    @Timed
    public ResponseEntity<TypePieceDTO> createTypePiece(@Valid @RequestBody TypePieceDTO typePieceDTO) throws URISyntaxException {
        log.debug("REST request to save TypePiece : {}", typePieceDTO);
        if (typePieceDTO.getId() != null) {
            throw new BadRequestAlertException("A new typePiece cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TypePieceDTO result = typePieceService.save(typePieceDTO);
        return ResponseEntity.created(new URI("/api/type-pieces/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /type-pieces : Updates an existing typePiece.
     *
     * @param typePieceDTO the typePieceDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated typePieceDTO,
     * or with status 400 (Bad Request) if the typePieceDTO is not valid,
     * or with status 500 (Internal Server Error) if the typePieceDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/type-pieces")
    @Timed
    public ResponseEntity<TypePieceDTO> updateTypePiece(@Valid @RequestBody TypePieceDTO typePieceDTO) throws URISyntaxException {
        log.debug("REST request to update TypePiece : {}", typePieceDTO);
        if (typePieceDTO.getId() == null) {
            return createTypePiece(typePieceDTO);
        }


        TypePieceDTO result = typePieceService.save(typePieceDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, typePieceDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /type-pieces : get all the typePieces.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of typePieces in body
     */
    @GetMapping("/type-pieces")
    @Timed
    public ResponseEntity<List<TypePieceDTO>> getAllTypePieces(Pageable pageable) {
        log.debug("REST request to get a page of TypePieces");
        Page<TypePieceDTO> page = typePieceService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/type-pieces");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /type-pieces/:id : get the "id" typePiece.
     *
     * @param id the id of the typePieceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the typePieceDTO, or with status 404 (Not Found)
     */
    @GetMapping("/type-pieces/{id}")
    @Timed
    public ResponseEntity<TypePieceDTO> getTypePiece(@PathVariable Long id) {
        log.debug("REST request to get TypePiece : {}", id);
        TypePieceDTO typePieceDTO = typePieceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(typePieceDTO));
    }

    /**
     * DELETE  /type-pieces/:id : delete the "id" typePiece.
     *
     * @param id the id of the typePieceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/type-pieces/{id}")
    @Timed
    public ResponseEntity<Void> deleteTypePiece(@PathVariable Long id) {
        log.debug("REST request to delete TypePiece : {}", id);
        typePieceService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
