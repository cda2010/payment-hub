package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.GroupeProfilActeur;
import com.m2i.pti.domain.ProfilActeur;
import com.m2i.pti.domain.enumeration.TypeProfilActeur;
import com.m2i.pti.service.GroupeProfilActeurService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.GroupeProfilActeurDTO;
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
 * REST controller for managing GroupeProfilActeur.
 */
@RestController
@RequestMapping("/api")
public class GroupeProfilActeurResource {

    private final Logger log = LoggerFactory.getLogger(GroupeProfilActeurResource.class);

    private static final String ENTITY_NAME = "groupeProfilActeur";

    private final GroupeProfilActeurService groupeProfilActeurService;

    public GroupeProfilActeurResource(GroupeProfilActeurService groupeProfilActeurService) {
        this.groupeProfilActeurService = groupeProfilActeurService;
    }

    /**
     * POST  /groupe-profil-acteurs : Create a new groupeProfilActeur.
     *
     * @param groupeProfilActeurDTO the groupeProfilActeurDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new groupeProfilActeurDTO, or with status 400 (Bad Request) if the groupeProfilActeur has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/groupe-profil-acteurs")
    @Timed
    public ResponseEntity<GroupeProfilActeurDTO> createGroupeProfilActeur(@RequestBody GroupeProfilActeurDTO groupeProfilActeurDTO) throws URISyntaxException {
        log.debug("REST request to save GroupeProfilActeur : {}", groupeProfilActeurDTO);
        if (groupeProfilActeurDTO.getId() != null) {
            throw new BadRequestAlertException("A new groupeProfilActeur cannot already have an ID", ENTITY_NAME, "idexists");
        }
        GroupeProfilActeurDTO result = groupeProfilActeurService.save(groupeProfilActeurDTO);
        return ResponseEntity.created(new URI("/api/groupe-profil-acteurs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /groupe-profil-acteurs : Updates an existing groupeProfilActeur.
     *
     * @param groupeProfilActeurDTO the groupeProfilActeurDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated groupeProfilActeurDTO,
     * or with status 400 (Bad Request) if the groupeProfilActeurDTO is not valid,
     * or with status 500 (Internal Server Error) if the groupeProfilActeurDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/groupe-profil-acteurs")
    @Timed
    public ResponseEntity<GroupeProfilActeurDTO> updateGroupeProfilActeur(@RequestBody GroupeProfilActeurDTO groupeProfilActeurDTO) throws URISyntaxException {
        log.debug("REST request to update GroupeProfilActeur : {}", groupeProfilActeurDTO);
        if (groupeProfilActeurDTO.getId() == null) {
            return createGroupeProfilActeur(groupeProfilActeurDTO);
        }
        GroupeProfilActeurDTO result = groupeProfilActeurService.save(groupeProfilActeurDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, groupeProfilActeurDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /groupe-profil-acteurs : get all the groupeProfilActeurs.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of groupeProfilActeurs in body
     */
    @GetMapping("/groupe-profil-acteurs")
    @Timed
    public ResponseEntity<List<GroupeProfilActeurDTO>> getAllGroupeProfilActeurs(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of GroupeProfilActeurs");
        Page<GroupeProfilActeurDTO> page = groupeProfilActeurService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/groupe-profil-acteurs");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /groupe-profil-acteurs/:id : get the "id" groupeProfilActeur.
     *
     * @param id the id of the groupeProfilActeurDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the groupeProfilActeurDTO, or with status 404 (Not Found)
     */
    @GetMapping("/groupe-profil-acteurs/{id}")
    @Timed
    public ResponseEntity<GroupeProfilActeurDTO> getGroupeProfilActeur(@PathVariable Long id) {
        log.debug("REST request to get GroupeProfilActeur : {}", id);
        GroupeProfilActeurDTO groupeProfilActeurDTO = groupeProfilActeurService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(groupeProfilActeurDTO));
    }
    @GetMapping("/groupe-profil-acteurs/by-type/{acteur}")
    @Timed
    public List<GroupeProfilActeur> getAllProfilActeurByType(@PathVariable String acteur) {
        List<GroupeProfilActeur> list = groupeProfilActeurService.findAllByType(acteur);
        return list ;
    }
    /**
     * DELETE  /groupe-profil-acteurs/:id : delete the "id" groupeProfilActeur.
     *
     * @param id the id of the groupeProfilActeurDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/groupe-profil-acteurs/{id}")
    @Timed
    public ResponseEntity<Void> deleteGroupeProfilActeur(@PathVariable Long id) {
        log.debug("REST request to delete GroupeProfilActeur : {}", id);
        groupeProfilActeurService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
