package com.m2i.pti.web.rest;

import com.m2i.pti.service.TerminalService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.service.dto.TerminalDTO;
import com.m2i.pti.service.dto.TypeCompteDTO;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;


import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.m2i.pti.domain.Terminal}.
 */
@RestController
@RequestMapping("/api")
public class TerminalResource {

    private final Logger log = LoggerFactory.getLogger(TerminalResource.class);

    private static final String ENTITY_NAME = "terminal";

   

    private final TerminalService terminalService;

    public TerminalResource(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    /**
     * {@code POST  /terminals} : Create a new terminal.
     *
     * @param terminalDTO the terminalDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new terminalDTO, or with status {@code 400 (Bad Request)} if the terminal has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/terminals")
    public ResponseEntity<TerminalDTO> createTerminal(@RequestBody TerminalDTO terminalDTO) throws URISyntaxException {
        log.debug("REST request to save Terminal : {}", terminalDTO);
        if (terminalDTO.getId() != null) {
            throw new BadRequestAlertException("A new terminal cannot already have an ID", ENTITY_NAME, "idexists");
        }
       
        
        TerminalDTO result = terminalService.save(terminalDTO);
        return ResponseEntity.created(new URI("/api/terminals/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /terminals} : Updates an existing terminal.
     *
     * @param terminalDTO the terminalDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated terminalDTO,
     * or with status {@code 400 (Bad Request)} if the terminalDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the terminalDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/terminals")
    public ResponseEntity<TerminalDTO> updateTerminal(@RequestBody TerminalDTO terminalDTO) throws URISyntaxException {
        log.debug("REST request to update Terminal : {}", terminalDTO);
        if (terminalDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
      
        
         TerminalDTO result = terminalService.save(terminalDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, terminalDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /terminals} : get all the terminals.
     *
     * @param pageable the pagination information.
     * @param queryParams a {@link MultiValueMap} query parameters.
     * @param uriBuilder a {@link UriComponentsBuilder} URI builder.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of terminals in body.
     */
    @GetMapping("/terminals")
    public ResponseEntity<List<TerminalDTO>> getAllTerminals(Pageable pageable, @RequestParam MultiValueMap<String, String> queryParams, UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get a page of Terminals");
        Page<TerminalDTO> page = terminalService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/terminals");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * {@code GET  /terminals/:id} : get the "id" terminal.
     *
     * @param id the id of the terminalDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the terminalDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/terminals/{id}")
    public ResponseEntity<TerminalDTO> getTerminal(@PathVariable Long id) {
        log.debug("REST request to get Terminal : {}", id);
      TerminalDTO terminalDTO = terminalService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(terminalDTO));
    }

    /**
     * {@code DELETE  /terminals/:id} : delete the "id" terminal.
     *
     * @param id the id of the terminalDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/terminals/{id}")
    public ResponseEntity<Void> deleteTerminal(@PathVariable Long id) {
        log.debug("REST request to delete Terminal : {}", id);
        terminalService.delete(id);
         return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
