package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Authority;
import com.m2i.pti.service.RoleValidationService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.RoleValidationDTO;
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
 * REST controller for managing RoleValidation.
 */
@RestController
@RequestMapping("/api")
public class RoleValidationResource {

    private final Logger log = LoggerFactory.getLogger(RoleValidationResource.class);

    private static final String ENTITY_NAME = "roleValidation";

    private final RoleValidationService roleValidationService;

    public RoleValidationResource(RoleValidationService roleValidationService) {
        this.roleValidationService = roleValidationService;
    }

    /**
     * POST  /role-validations : Create a new roleValidation.
     *
     * @param roleValidationDTO the roleValidationDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new roleValidationDTO, or with status 400 (Bad Request) if the roleValidation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/role-validations")
    @Timed
    public ResponseEntity<RoleValidationDTO> createRoleValidation(@RequestBody RoleValidationDTO roleValidationDTO) throws URISyntaxException {
        log.debug("REST request to save RoleValidation : {}", roleValidationDTO);
        if (roleValidationDTO.getId() != null) {
            throw new BadRequestAlertException("A new roleValidation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RoleValidationDTO result = roleValidationService.save(roleValidationDTO);
        return ResponseEntity.created(new URI("/api/role-validations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /role-validations : Updates an existing roleValidation.
     *
     * @param roleValidationDTO the roleValidationDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated roleValidationDTO,
     * or with status 400 (Bad Request) if the roleValidationDTO is not valid,
     * or with status 500 (Internal Server Error) if the roleValidationDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/role-validations")
    @Timed
    public ResponseEntity<RoleValidationDTO> updateRoleValidation(@RequestBody RoleValidationDTO roleValidationDTO) throws URISyntaxException {
        log.debug("REST request to update RoleValidation : {}", roleValidationDTO);
        if (roleValidationDTO.getId() == null) {
            return createRoleValidation(roleValidationDTO);
        }
        RoleValidationDTO result = roleValidationService.save(roleValidationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, roleValidationDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /role-validations : get all the roleValidations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of roleValidations in body
     */
    @GetMapping("/role-validations")
    @Timed
    public ResponseEntity<List<RoleValidationDTO>> getAllRoleValidations(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of RoleValidations");
        Page<RoleValidationDTO> page = roleValidationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/role-validations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    @GetMapping("/role-validations/authorities")
    @Timed
    public List<Authority> findAllAuthorities(){
        return roleValidationService.findAllAuthorities();
    }

    /**
     * GET  /role-validations/:id : get the "id" roleValidation.
     *
     * @param id the id of the roleValidationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the roleValidationDTO, or with status 404 (Not Found)
     */
    @GetMapping("/role-validations/{id}")
    @Timed
    public ResponseEntity<RoleValidationDTO> getRoleValidation(@PathVariable Long id) {
        log.debug("REST request to get RoleValidation : {}", id);
        RoleValidationDTO roleValidationDTO = roleValidationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(roleValidationDTO));
    }

    /**
     * DELETE  /role-validations/:id : delete the "id" roleValidation.
     *
     * @param id the id of the roleValidationDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/role-validations/{id}")
    @Timed
    public ResponseEntity<Void> deleteRoleValidation(@PathVariable Long id) {
        log.debug("REST request to delete RoleValidation : {}", id);
        roleValidationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
