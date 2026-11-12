package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Authority;
import com.m2i.pti.service.TypeUniteOrganisationService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.TypeUniteOrganisationDTO;
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
import java.util.Set;

/**
 * REST controller for managing TypeUniteOrganisation.
 */
@RestController
@RequestMapping("/api")
public class TypeUniteOrganisationResource {

    private final Logger log = LoggerFactory.getLogger(TypeUniteOrganisationResource.class);

    private static final String ENTITY_NAME = "typeUniteOrganisation";

    private final TypeUniteOrganisationService typeUniteOrganisationService;

    public TypeUniteOrganisationResource(TypeUniteOrganisationService typeUniteOrganisationService) {
        this.typeUniteOrganisationService = typeUniteOrganisationService;
    }

    /**
     * POST  /type-unite-organisations : Create a new typeUniteOrganisation.
     *
     * @param typeUniteOrganisationDTO the typeUniteOrganisationDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new typeUniteOrganisationDTO, or with status 400 (Bad Request) if the typeUniteOrganisation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/type-unite-organisations")
    @Timed
    public ResponseEntity<TypeUniteOrganisationDTO> createTypeUniteOrganisation(@RequestBody TypeUniteOrganisationDTO typeUniteOrganisationDTO) throws URISyntaxException {
        log.debug("REST request to save TypeUniteOrganisation : {}", typeUniteOrganisationDTO);
        if (typeUniteOrganisationDTO.getId() != null) {
            throw new BadRequestAlertException("A new typeUniteOrganisation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TypeUniteOrganisationDTO result = typeUniteOrganisationService.save(typeUniteOrganisationDTO);
        return ResponseEntity.created(new URI("/api/type-unite-organisations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /type-unite-organisations : Updates an existing typeUniteOrganisation.
     *
     * @param typeUniteOrganisationDTO the typeUniteOrganisationDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated typeUniteOrganisationDTO,
     * or with status 400 (Bad Request) if the typeUniteOrganisationDTO is not valid,
     * or with status 500 (Internal Server Error) if the typeUniteOrganisationDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/type-unite-organisations")
    @Timed
    public ResponseEntity<TypeUniteOrganisationDTO> updateTypeUniteOrganisation(@RequestBody TypeUniteOrganisationDTO typeUniteOrganisationDTO) throws URISyntaxException {
        log.debug("REST request to update TypeUniteOrganisation : {}", typeUniteOrganisationDTO);
        if (typeUniteOrganisationDTO.getId() == null) {
            return createTypeUniteOrganisation(typeUniteOrganisationDTO);
        }
        TypeUniteOrganisationDTO result = typeUniteOrganisationService.save(typeUniteOrganisationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, typeUniteOrganisationDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /type-unite-organisations : get all the typeUniteOrganisations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of typeUniteOrganisations in body
     */
    @GetMapping("/type-unite-organisations")
    @Timed
    public ResponseEntity<List<TypeUniteOrganisationDTO>> getAllTypeUniteOrganisations(Pageable pageable) {
        log.debug("REST request to get a page of TypeUniteOrganisations");
        Page<TypeUniteOrganisationDTO> page = typeUniteOrganisationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/type-unite-organisations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /type-unite-organisations : get all the typeUniteOrganisations without Unite de regroupement.
     *
     *
     * @return the ResponseEntity with status 200 (OK) and the list of typeUniteOrganisations in body
     */
    @GetMapping("/type-unite-organisationsWithoutUR")
    @Timed
    public List<TypeUniteOrganisationDTO> getTypeUniteOrganisationsWithoutUR() {
        log.debug("REST request to get a page of TypeUniteOrganisations WithoutUR");
        List<TypeUniteOrganisationDTO> list = typeUniteOrganisationService.findAllWithoutUR();
        return list;
    }

    /**
     * GET  /type-unite-organisations/:id : get the "id" typeUniteOrganisation.
     *
     * @param id the id of the typeUniteOrganisationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the typeUniteOrganisationDTO, or with status 404 (Not Found)
     */
    @GetMapping("/type-unite-organisations/{id}")
    @Timed
    public ResponseEntity<TypeUniteOrganisationDTO> getTypeUniteOrganisation(@PathVariable Long id) {
        log.debug("REST request to get TypeUniteOrganisation : {}", id);
        TypeUniteOrganisationDTO typeUniteOrganisationDTO = typeUniteOrganisationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(typeUniteOrganisationDTO));
    }

    /**
     * GET  /type-uniteOrganisations/:id/roles : get the "id" typeuniteOrganisation.
     *
     * @param id the id of the typeuniteOrganisationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the typeuniteOrganisationDTO, or with status 404 (Not Found)
     */
    @GetMapping("/type-unite-organisations/{id}/roles")
    @Timed
    public Set<Authority> getRolesByTypeUniteOrganisation(@PathVariable Long id) {
        log.debug("REST request to get TypeuniteOrganisation : {}", id);
        return typeUniteOrganisationService.findAllRoleByTypeUniteOrganisation(id);
    }

    /**
     * DELETE  /type-unite-organisations/:id : delete the "id" typeUniteOrganisation.
     *
     * @param id the id of the typeUniteOrganisationDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/type-unite-organisations/{id}")
    @Timed
    public ResponseEntity<Void> deleteTypeUniteOrganisation(@PathVariable Long id) {
        log.debug("REST request to delete TypeUniteOrganisation : {}", id);
        typeUniteOrganisationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/type-unite-organisations-optimise")
    @Timed
    public ResponseEntity<List<TypeUniteOrganisationDTO>> getAllTypeUniteOrganisationsOptimise(Pageable pageable) {
        log.debug("REST request to get a page of TypeUniteOrganisations");
        Page<TypeUniteOrganisationDTO> page = typeUniteOrganisationService.findAllOptimise(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/type-unite-organisations-optimise");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    @GetMapping("/type-unite-organisationsWithoutUR-optimise")
    @Timed
    public List<TypeUniteOrganisationDTO> getTypeUniteOrganisationsWithoutUROptimise() {
        log.debug("REST request to get a page of TypeUniteOrganisations WithoutUR");
        List<TypeUniteOrganisationDTO> list = typeUniteOrganisationService.findAllWithoutUROptimise();
        return list;
    }
    @GetMapping("/type-unite-organisations-optimise/{id}")
    @Timed
    public ResponseEntity<TypeUniteOrganisationDTO> getTypeUniteOrganisationOptimise(@PathVariable Long id) {
        log.debug("REST request to get TypeUniteOrganisation : {}", id);
        TypeUniteOrganisationDTO typeUniteOrganisationDTO = typeUniteOrganisationService.findOneOptimise(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(typeUniteOrganisationDTO));
    }

 @GetMapping("/type-unite-organisations-optimise/type")
    @Timed
    public TypeUniteOrganisationDTO getTypeUniteOrganisationByUoID(@RequestParam("id") Long id) {
        log.debug("REST request to get TypeUniteOrganisation : {}", id);
        TypeUniteOrganisationDTO typeUniteOrganisationDTO = typeUniteOrganisationService.findTypeUniteOrganisationByUo(id) ;
        return typeUniteOrganisationDTO ;
    }


}
