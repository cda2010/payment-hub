package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.enumeration.InstructionEcriture;
import com.m2i.pti.domain.enumeration.MontantsAComptabiliser;
import com.m2i.pti.service.InstructionComptableService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.InstructionComptableDTO;
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
 * REST controller for managing InstructionComptable.
 */
@RestController
@RequestMapping("/api")
public class  InstructionComptableResource {

    private final Logger log = LoggerFactory.getLogger(InstructionComptableResource.class);

    private static final String ENTITY_NAME = "instructionComptable";

    private final InstructionComptableService instructionComptableService;

    public InstructionComptableResource(InstructionComptableService instructionComptableService) {
        this.instructionComptableService = instructionComptableService;
    }

    /**
     * POST  /instruction-comptables : Create a new instructionComptable.
     *
     * @param instructionComptableDTO the instructionComptableDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new instructionComptableDTO, or with status 400 (Bad Request) if the instructionComptable has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/instruction-comptables")
    @Timed
    public ResponseEntity<InstructionComptableDTO> createInstructionComptable(@RequestBody InstructionComptableDTO instructionComptableDTO) throws URISyntaxException {
        log.debug("REST request to save InstructionComptable : {}", instructionComptableDTO);
        if (instructionComptableDTO.getId() != null) {
            throw new BadRequestAlertException("A new instructionComptable cannot already have an ID", ENTITY_NAME, "idexists");
        }
        InstructionComptableDTO result = instructionComptableService.save(instructionComptableDTO);
        return ResponseEntity.created(new URI("/api/instruction-comptables/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /instruction-comptables : Updates an existing instructionComptable.
     *
     * @param instructionComptableDTO the instructionComptableDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated instructionComptableDTO,
     * or with status 400 (Bad Request) if the instructionComptableDTO is not valid,
     * or with status 500 (Internal Server Error) if the instructionComptableDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/instruction-comptables")
    @Timed
    public ResponseEntity<InstructionComptableDTO> updateInstructionComptable(@RequestBody InstructionComptableDTO instructionComptableDTO) throws URISyntaxException {
        log.debug("REST request to update InstructionComptable : {}", instructionComptableDTO);
        if (instructionComptableDTO.getId() == null) {
            return createInstructionComptable(instructionComptableDTO);
        }
        InstructionComptableDTO result = instructionComptableService.save(instructionComptableDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, instructionComptableDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /instruction-comptables : get all the instructionComptables.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of instructionComptables in body
     */
    @GetMapping("/instruction-comptables")
    @Timed
    public ResponseEntity<List<InstructionComptableDTO>> getAllInstructionComptables(Pageable pageable, @RequestParam(required = false)Long typeServiceId, @RequestParam(required = false)InstructionEcriture instruction, @RequestParam(required = false)String etiquetteComptable, @RequestParam(required = false)MontantsAComptabiliser montant) {
        log.debug("REST request to get a page of InstructionComptables");
        Page<InstructionComptableDTO> page = instructionComptableService.findAllByMultiParams(pageable,typeServiceId,instruction,etiquetteComptable,montant);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/instruction-comptables");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /instruction-comptables/:id : get the "id" instructionComptable.
     *
     * @param id the id of the instructionComptableDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the instructionComptableDTO, or with status 404 (Not Found)
     */
    @GetMapping("/instruction-comptables/{id}")
    @Timed
    public ResponseEntity<InstructionComptableDTO> getInstructionComptable(@PathVariable Long id) {
        log.debug("REST request to get InstructionComptable : {}", id);
        InstructionComptableDTO instructionComptableDTO = instructionComptableService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(instructionComptableDTO));
    }

    /**
     * DELETE  /instruction-comptables/:id : delete the "id" instructionComptable.
     *
     * @param id the id of the instructionComptableDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/instruction-comptables/{id}")
    @Timed
    public ResponseEntity<Void> deleteInstructionComptable(@PathVariable Long id) {
        log.debug("REST request to delete InstructionComptable : {}", id);
        instructionComptableService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
