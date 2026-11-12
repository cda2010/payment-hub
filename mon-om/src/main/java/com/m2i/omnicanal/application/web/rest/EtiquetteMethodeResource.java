package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.Etiquette;
import com.m2i.omnicanal.application.domain.EtiquetteMethode;
import com.m2i.omnicanal.application.repository.EtiquetteMethodeRepository;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;
import com.m2i.omnicanal.application.web.rest.util.PaginationUtil;
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
 * REST controller for managing EtiquetteMethode.
 */
@RestController
@RequestMapping("/api")
public class EtiquetteMethodeResource {

    private final Logger log = LoggerFactory.getLogger(EtiquetteMethodeResource.class);

    private static final String ENTITY_NAME = "etiquetteMethode";

    private final EtiquetteMethodeRepository etiquetteMethodeRepository;

    public EtiquetteMethodeResource(EtiquetteMethodeRepository etiquetteMethodeRepository) {
        this.etiquetteMethodeRepository = etiquetteMethodeRepository;
    }

    /**
     * POST  /etiquettes-methodes : Create a new etiquetteMethode.
     *
     * @param etiquetteMethode the etiquetteMethode to create
     * @return the ResponseEntity with status 201 (Created) and with body the new etiquetteMethode, or with status 400 (Bad Request) if the etiquetteMethode has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/etiquettes-methodes")
    @Timed
    public ResponseEntity<EtiquetteMethode> createEtiquette(@RequestBody EtiquetteMethode etiquetteMethode) throws URISyntaxException {
        log.debug("REST request to save EtiquetteMethode : {}", etiquetteMethode);
        if (etiquetteMethode.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new etiquetteMethode cannot already have an ID")).body(null);
        }
        EtiquetteMethode result = etiquetteMethodeRepository.save(etiquetteMethode);
        return ResponseEntity.created(new URI("/api/etiquettes-methodes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /etiquettes-methodes : Updates an existing etiquetteMethode.
     *
     * @param etiquetteMethode the etiquetteMethode to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated etiquetteMethode,
     * or with status 400 (Bad Request) if the etiquetteMethode is not valid,
     * or with status 500 (Internal Server Error) if the etiquetteMethode couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/etiquettes-methodes")
    @Timed
    public ResponseEntity<EtiquetteMethode> updateEtiquette(@RequestBody EtiquetteMethode etiquetteMethode) throws URISyntaxException {
        log.debug("REST request to update EtiquetteMethode : {}", etiquetteMethode);
        if (etiquetteMethode.getId() == null) {
            return createEtiquette(etiquetteMethode);
        }
        EtiquetteMethode result = etiquetteMethodeRepository.save(etiquetteMethode);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, etiquetteMethode.getId().toString()))
            .body(result);
    }

    /**
     * GET  /etiquettes-methodes : get all the etiquettes-methodes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of etiquettes-methodes in body
     */
    @GetMapping("/etiquettes-methodes")
    @Timed
//    public List<EtiquetteMethode> getAllEtiquettes() {
//        log.debug("REST request to get all Etiquettes");
//        List<EtiquetteMethode> etiquettes = etiquetteMethodeRepository.findAll();
//        return etiquettes;
//    }
    public ResponseEntity<List<EtiquetteMethode>> getAllEtiquettes(Pageable pageable) {
        log.debug("REST request to get all Etiquettes");
        Page<EtiquetteMethode> page = etiquetteMethodeRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/etiquettes-methodes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);

    }

    /**
     * GET  /etiquettes-methodes/:id : get the "id" etiquetteMethode.
     *
     * @param id the id of the etiquetteMethode to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the etiquetteMethode, or with status 404 (Not Found)
     */
    @GetMapping("/etiquettes-methodes/{id}")
    @Timed
    public ResponseEntity<EtiquetteMethode> getEtiquette(@PathVariable Long id) {
        log.debug("REST request to get EtiquetteMethode : {}", id);
        EtiquetteMethode etiquetteMethode = etiquetteMethodeRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(etiquetteMethode));
    }

    /**
     * DELETE  /etiquettes-methodes/:id : delete the "id" etiquetteMethode.
     *
     * @param id the id of the etiquetteMethode to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/etiquettes-methodes/{id}")
    @Timed
    public ResponseEntity<Void> deleteEtiquette(@PathVariable Long id) {
        log.debug("REST request to delete EtiquetteMethode : {}", id);
        etiquetteMethodeRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
