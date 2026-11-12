package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.AutreParametreFSE;

import com.m2i.omnicanal.application.repository.AutreParametreRepository;
import com.m2i.omnicanal.application.service.AutreParametreService;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;
import com.m2i.omnicanal.application.web.rest.util.PaginationUtil;
import com.m2i.omnicanal.model.AutreParametre;

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
 * REST controller for managing AutreParametre.
 */
@RestController
@RequestMapping("/api")
public class AutreParametreResource {

    private final Logger log = LoggerFactory.getLogger(AutreParametreResource.class);

    private static final String ENTITY_NAME = "autreParametre";

    private final AutreParametreService autreParametreService;
    private final AutreParametreRepository APRepository;

    public AutreParametreResource(AutreParametreService autreParametreService, AutreParametreRepository apRepository) {
        this.autreParametreService = autreParametreService;
        APRepository = apRepository;
    }

    /**
     * POST  /autre-parametres : Create a new autreParametre.
     *
     * @param autreParametre the autreParametre to create
     * @return the ResponseEntity with status 201 (Created) and with body the new autreParametre, or with status 400 (Bad Request) if the autreParametre has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/autre-parametres")
    @Timed
    public ResponseEntity<AutreParametre> createAutreParametre(@Valid @RequestBody AutreParametreFSE autreParametre) throws URISyntaxException {
        log.debug("REST request to save AutreParametre : {}", autreParametre);
        if (autreParametre.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new autreParametre cannot already have an ID")).body(null);
        }
        AutreParametre result = autreParametreService.save(autreParametre);
        return ResponseEntity.created(new URI("/api/autre-parametres/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /autre-parametres : Updates an existing autreParametre.
     *
     * @param autreParametre the autreParametre to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated autreParametre,
     * or with status 400 (Bad Request) if the autreParametre is not valid,
     * or with status 500 (Internal Server Error) if the autreParametre couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/autre-parametres")
    @Timed
    public ResponseEntity<AutreParametre> updateAutreParametre(@Valid @RequestBody AutreParametreFSE autreParametre) throws URISyntaxException {
        log.debug("REST request to update AutreParametre : {}", autreParametre);
        if (autreParametre.getId() == null) {
            return createAutreParametre(autreParametre);
        }
        AutreParametre result = autreParametreService.save(autreParametre);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, autreParametre.getId().toString()))
            .body(result);
    }

    /**
     * GET  /autre-parametres : get all the autreParametres.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of autreParametres in body
     */
    @GetMapping("/autre-parametres")
    @Timed
//    public List<AutreParametre> getAllAutreParametres() {
//        log.debug("REST request to get all AutreParametres");
//        return autreParametreService.findAll();
//    }

    public ResponseEntity<List<AutreParametre>> getAllAutreParametres(Pageable pageable) {
        log.debug("REST request to get all AutreParametres");
        Page<AutreParametre> page = APRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/apis");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);

    }

    /**
     * GET  /autre-parametres/:id : get the "id" autreParametre.
     *
     * @param id the id of the autreParametre to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the autreParametre, or with status 404 (Not Found)
     */
    @GetMapping("/autre-parametres/{id}")
    @Timed
    public ResponseEntity<AutreParametre> getAutreParametre(@PathVariable Long id) {
        log.debug("REST request to get AutreParametre : {}", id);
        AutreParametre autreParametre = autreParametreService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(autreParametre));
    }

    /**
     * DELETE  /autre-parametres/:id : delete the "id" autreParametre.
     *
     * @param id the id of the autreParametre to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/autre-parametres/{id}")
    @Timed
    public ResponseEntity<Void> deleteAutreParametre(@PathVariable Long id) {
        log.debug("REST request to delete AutreParametre : {}", id);
        autreParametreService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
