package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Ecran;

import com.m2i.pti.repository.EcranRepository;
import com.m2i.pti.service.dto.EcranListeDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Ecran.
 */
@RestController
@RequestMapping("/api")
public class EcranResource {

    private final Logger log = LoggerFactory.getLogger(EcranResource.class);

    private static final String ENTITY_NAME = "ecran";

    private final EcranRepository ecranRepository;

    public EcranResource(EcranRepository ecranRepository) {
        this.ecranRepository = ecranRepository;
    }

    /**
     * POST  /ecrans : Create a new ecran.
     *
     * @param ecran the ecran to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ecran, or with status 400 (Bad Request) if the ecran has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/ecrans")
    @Timed
    public ResponseEntity<Ecran> createEcran(@RequestBody Ecran ecran) throws URISyntaxException {
        log.debug("REST request to save Ecran : {}", ecran);
        if (ecran.getId() != null) {
            throw new BadRequestAlertException("A new ecran cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Ecran result = ecranRepository.save(ecran);
        return ResponseEntity.created(new URI("/api/ecrans/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /ecrans : Updates an existing ecran.
     *
     * @param ecran the ecran to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ecran,
     * or with status 400 (Bad Request) if the ecran is not valid,
     * or with status 500 (Internal Server Error) if the ecran couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/ecrans")
    @Timed
    public ResponseEntity<Ecran> updateEcran(@RequestBody Ecran ecran) throws URISyntaxException {
        log.debug("REST request to update Ecran : {}", ecran);
        if (ecran.getId() == null) {
            return createEcran(ecran);
        }
        Ecran result = ecranRepository.save(ecran);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ecran.getId().toString()))
            .body(result);
    }

    /**
     * GET  /ecrans : get all the ecrans.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of ecrans in body
     */
    @GetMapping("/ecrans")
    @Timed
    public List<Ecran> getAllEcrans() {
        log.debug("REST request to get all Ecrans");
        return ecranRepository.findAll();
        }

    /**
     * GET  /ecrans/:id : get the "id" ecran.
     *
     * @param id the id of the ecran to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ecran, or with status 404 (Not Found)
     */
    @GetMapping("/ecrans/{id}")
    @Timed
    public ResponseEntity<Ecran> getEcran(@PathVariable Long id) {
        log.debug("REST request to get Ecran : {}", id);
        Ecran ecran = ecranRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ecran));
    }

    @GetMapping("/ecrans/of-produit/{id}/liste")
    @Timed
    public List<EcranListeDTO> getEcranListe(@PathVariable Long id) {
        return ecranRepository.findListeByProduit(id);
    }

    @GetMapping("/ecrans/of-produit/{id}")
    @Timed
    public List<Ecran> getEcransByProduitId(@PathVariable Long id) {

        return ecranRepository.findEcransByProduitIdOrderByNumeroOrdreAsc(id);
    }

    /**
     * DELETE  /ecrans/:id : delete the "id" ecran.
     *
     * @param id the id of the ecran to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/ecrans/{id}")
    @Timed
    public ResponseEntity<Void> deleteEcran(@PathVariable Long id) {
        log.debug("REST request to delete Ecran : {}", id);
        ecranRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
