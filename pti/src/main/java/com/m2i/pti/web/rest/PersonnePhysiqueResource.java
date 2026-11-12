package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.PersonnePhysiqueService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.PersonnePhysiqueDTO;
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
 * REST controller for managing PersonnePhysique.
 */
@RestController
@RequestMapping("/api")
public class PersonnePhysiqueResource {

    private final Logger log = LoggerFactory.getLogger(PersonnePhysiqueResource.class);

    private static final String ENTITY_NAME = "personnePhysique";

    private final PersonnePhysiqueService personnePhysiqueService;

    public PersonnePhysiqueResource(PersonnePhysiqueService personnePhysiqueService) {
        this.personnePhysiqueService = personnePhysiqueService;
    }

    /**
     * POST  /personne-physiques : Create a new personnePhysique.
     *
     * @param personnePhysiqueDTO the personnePhysiqueDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new personnePhysiqueDTO, or with status 400 (Bad Request) if the personnePhysique has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/personne-physiques")
    @Timed
    public ResponseEntity<PersonnePhysiqueDTO> createPersonnePhysique(@Valid @RequestBody PersonnePhysiqueDTO personnePhysiqueDTO) throws URISyntaxException {
        log.debug("REST request to save PersonnePhysique : {}", personnePhysiqueDTO);
        if (personnePhysiqueDTO.getId() != null) {
            throw new BadRequestAlertException("A new personnePhysique cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PersonnePhysiqueDTO result = personnePhysiqueService.save(personnePhysiqueDTO);
        return ResponseEntity.created(new URI("/api/personne-physiques/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /personne-physiques : Updates an existing personnePhysique.
     *
     * @param personnePhysiqueDTO the personnePhysiqueDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated personnePhysiqueDTO,
     * or with status 400 (Bad Request) if the personnePhysiqueDTO is not valid,
     * or with status 500 (Internal Server Error) if the personnePhysiqueDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/personne-physiques")
    @Timed
    public ResponseEntity<PersonnePhysiqueDTO> updatePersonnePhysique(@Valid @RequestBody PersonnePhysiqueDTO personnePhysiqueDTO) throws URISyntaxException {
        log.debug("REST request to update PersonnePhysique : {}", personnePhysiqueDTO);
        if (personnePhysiqueDTO.getId() == null) {
            return createPersonnePhysique(personnePhysiqueDTO);
        }
        PersonnePhysiqueDTO result = personnePhysiqueService.save(personnePhysiqueDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, personnePhysiqueDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /personne-physiques : get all the personnePhysiques.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of personnePhysiques in body
     */
    @GetMapping("/personne-physiques")
    @Timed
    public ResponseEntity<List<PersonnePhysiqueDTO>> getAllPersonnePhysiques(Pageable pageable) {
        log.debug("REST request to get a page of PersonnePhysiques");
        Page<PersonnePhysiqueDTO> page = personnePhysiqueService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/personne-physiques");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /personne-physiques/:id : get the "id" personnePhysique.
     *
     * @param id the id of the personnePhysiqueDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the personnePhysiqueDTO, or with status 404 (Not Found)
     */
    @GetMapping("/personne-physiques/{id}")
    @Timed
    public ResponseEntity<PersonnePhysiqueDTO> getPersonnePhysique(@PathVariable Long id) {
        log.debug("REST request to get PersonnePhysique : {}", id);
        PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(personnePhysiqueDTO));
    }

    /**
     * DELETE  /personne-physiques/:id : delete the "id" personnePhysique.
     *
     * @param id the id of the personnePhysiqueDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/personne-physiques/{id}")
    @Timed
    public ResponseEntity<Void> deletePersonnePhysique(@PathVariable Long id) {
        log.debug("REST request to delete PersonnePhysique : {}", id);
        personnePhysiqueService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
