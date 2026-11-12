package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.PieceService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.PieceDTO;
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
 * REST controller for managing Piece.
 */
@RestController
@RequestMapping("/api")
public class PieceResource {

    private final Logger log = LoggerFactory.getLogger(PieceResource.class);

    private static final String ENTITY_NAME = "piece";

    private final PieceService pieceService;

    public PieceResource(PieceService pieceService) {
        this.pieceService = pieceService;
    }

    /**
     * POST  /pieces : Create a new piece.
     *
     * @param pieceDTO the pieceDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new pieceDTO, or with status 400 (Bad Request) if the piece has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/pieces")
    @Timed
    public ResponseEntity<PieceDTO> createPiece(@Valid @RequestBody PieceDTO pieceDTO) throws URISyntaxException {
        log.debug("REST request to save Piece : {}", pieceDTO);
        if (pieceDTO.getId() != null) {
            throw new BadRequestAlertException("A new piece cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PieceDTO result = pieceService.save(pieceDTO);
        return ResponseEntity.created(new URI("/api/pieces/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /pieces : Updates an existing piece.
     *
     * @param pieceDTO the pieceDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated pieceDTO,
     * or with status 400 (Bad Request) if the pieceDTO is not valid,
     * or with status 500 (Internal Server Error) if the pieceDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/pieces")
    @Timed
    public ResponseEntity<PieceDTO> updatePiece(@Valid @RequestBody PieceDTO pieceDTO) throws URISyntaxException {
        log.debug("REST request to update Piece : {}", pieceDTO);
        if (pieceDTO.getId() == null) {
            return createPiece(pieceDTO);
        }
        PieceDTO result = pieceService.save(pieceDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, pieceDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /pieces : get all the pieces.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of pieces in body
     */
    @GetMapping("/pieces")
    @Timed
    public ResponseEntity<List<PieceDTO>> getAllPieces(Pageable pageable) {
        log.debug("REST request to get a page of Pieces");
        Page<PieceDTO> page = pieceService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/pieces");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /pieces/:id : get the "id" piece.
     *
     * @param id the id of the pieceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the pieceDTO, or with status 404 (Not Found)
     */
    @GetMapping("/pieces/{id}")
    @Timed
    public ResponseEntity<PieceDTO> getPiece(@PathVariable Long id) {
        log.debug("REST request to get Piece : {}", id);
        PieceDTO pieceDTO = pieceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(pieceDTO));
    }

    /**
     * GET  /pieces/:id : get the "id" piece.
     *
     * @param id the id of the pieceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the pieceDTO, or with status 404 (Not Found)
     */
    @GetMapping("/piecesByTitulaireIdss/{id}")
    @Timed
    public ResponseEntity<PieceDTO> getPieceByTitulaireId(@PathVariable Long id) {
        log.debug("REST request to get Piece : {}", id);
        PieceDTO pieceDTO = pieceService.findOneByTitulaireId(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(pieceDTO));
    }

    @GetMapping("/piecesOfTitulaireId/{id}")
    @Timed
    public List<PieceDTO> getAllPiecesOfPersonne(@PathVariable Long id) {
        log.debug("REST request to get Piece : {}", id);
        return pieceService.findAllByTitulaireId(id);
    }

    /**
     * DELETE  /pieces/:id : delete the "id" piece.
     *
     * @param id the id of the pieceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/pieces/{id}")
    @Timed
    public ResponseEntity<Void> deletePiece(@PathVariable Long id) {
        log.debug("REST request to delete Piece : {}", id);
        pieceService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/piecesByTitulaireIds_new")
    @Timed
    public ResponseEntity<PieceDTO> getPieceByTitulaireIdNew(@RequestParam Long id) {
        log.debug("REST request to get Piece : {}", id);
        PieceDTO pieceDTO = pieceService.findOneByTitulaireId(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(pieceDTO));
    }
}
