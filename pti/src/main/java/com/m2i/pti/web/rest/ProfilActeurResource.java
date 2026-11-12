package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.ProfilActeur;
import com.m2i.pti.domain.enumeration.TypeProfilActeur;
import com.m2i.pti.service.ProfilActeurService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.ProfilActeurDTO;
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
 * REST controller for managing ProfilActeur.
 */
@RestController
@RequestMapping("/api")
public class ProfilActeurResource {

    private final Logger log = LoggerFactory.getLogger(ProfilActeurResource.class);

    private static final String ENTITY_NAME = "profilActeur";

    private final ProfilActeurService profilActeurService;

    public ProfilActeurResource(ProfilActeurService profilActeurService) {
        this.profilActeurService = profilActeurService;
    }

    /**
     * POST  /profil-acteurs : Create a new profilActeur.
     *
     * @param profilActeurDTO the profilActeurDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new profilActeurDTO, or with status 400 (Bad Request) if the profilActeur has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/profil-acteurs")
    @Timed
    public ResponseEntity<ProfilActeurDTO> createProfilActeur(@Valid @RequestBody ProfilActeur profilActeurDTO) throws URISyntaxException {
        log.debug("REST request to save ProfilActeur : {}", profilActeurDTO);
        if (profilActeurDTO.getId() != null) {
            throw new BadRequestAlertException("A new profilActeur cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ProfilActeurDTO result = profilActeurService.save(profilActeurDTO);
        return ResponseEntity.created(new URI("/api/profil-acteurs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /profil-acteurs : Updates an existing profilActeur.
     *
     * @param profilActeurDTO the profilActeurDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated profilActeurDTO,
     * or with status 400 (Bad Request) if the profilActeurDTO is not valid,
     * or with status 500 (Internal Server Error) if the profilActeurDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/profil-acteurs")
    @Timed
    public ResponseEntity<ProfilActeurDTO> updateProfilActeur(@Valid @RequestBody ProfilActeur profilActeurDTO) throws URISyntaxException {
        log.debug("REST request to update ProfilActeur : {}", profilActeurDTO);
        if (profilActeurDTO.getId() == null) {
            return createProfilActeur(profilActeurDTO);
        }
        this.profilActeurService.verifProfilActeur(profilActeurDTO);
        ProfilActeurDTO result = profilActeurService.save(profilActeurDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, profilActeurDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /profil-acteurs : get all the profilActeurs.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of profilActeurs in body
     */
    
    /**
     * GET  /profil-acteurs : get all the profilActeurs.
     * @param acteur
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of profilActeurs in body
     */
    @GetMapping("/profil-acteurs/acteur/{acteur}")
    @Timed
    public List<ProfilActeur> getAllProfilActeursCli(@PathVariable String acteur) {
        log.debug("REST request to get a page of ProfilActeurs"+acteur);
        TypeProfilActeur act=null;
       
                  act=  TypeProfilActeur.CLIENT;
           
        List<ProfilActeur> list = profilActeurService.findAllByType(act);
      
        return list ;
    }
    @GetMapping("/profil-acteurs")
    @Timed
    public ResponseEntity<List<ProfilActeur>> getAllProfilActeurs(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of ProfilActeurs");
        Page<ProfilActeur> page = profilActeurService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/profil-acteurs");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /profil-acteurs/:id : get the "id" profilActeur.
     *
     * @param id the id of the profilActeurDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the profilActeurDTO, or with status 404 (Not Found)
     */
    @GetMapping("/profil-acteurs/{id}")
    @Timed
    public ResponseEntity<ProfilActeur> getProfilActeur(@PathVariable Long id) {
        log.debug("REST request to get ProfilActeur : {}", id);
        ProfilActeur profilActeurDTO = profilActeurService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(profilActeurDTO));
    }

    @GetMapping("/profil-acteurs/by-groupe/{groupeId}")
    @Timed
    public List<ProfilActeur> findAllByGroupe(@PathVariable Long groupeId) {

        return profilActeurService.findAllByGroupe(groupeId);
    }

    /**
     * DELETE  /profil-acteurs/:id : delete the "id" profilActeur.
     *
     * @param id the id of the profilActeurDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/profil-acteurs/{id}")
    @Timed
    public ResponseEntity<Void> deleteProfilActeur(@PathVariable Long id) {
        log.debug("REST request to delete ProfilActeur : {}", id);
        profilActeurService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
