package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.presentation.domain.CompteFse;
import com.m2i.omnicanal.presentation.service.CompteService;
import com.m2i.omnicanal.presentation.web.rest.util.HeaderUtil;

import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing Compte.
 */
@RestController
@RequestMapping("/api")
public class CompteResource {

    private final Logger log = LoggerFactory.getLogger(CompteResource.class);

    private static final String ENTITY_NAME = "compte";

    private final CompteService compteService;

    public CompteResource(CompteService compteService) {
        this.compteService = compteService;
    }

    /**
     * POST /comptes : Create a new compte.
     *
     * @param compte the compte to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new compte, or with status 400 (Bad Request) if the compte has already an
     * ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/comptes")
    @Timed
    public ResponseEntity<CompteFse> createCompte(@RequestBody CompteFse compte) throws URISyntaxException {
        log.debug("REST request to save Compte : {}", compte);
        if (compte.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new compte cannot already have an ID")).body(null);
        }
        CompteFse result = compteService.save(compte);
        return ResponseEntity.created(new URI("/api/comptes/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT /comptes : Updates an existing compte.
     *
     * @param compte the compte to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated
     * compte, or with status 400 (Bad Request) if the compte is not valid, or
     * with status 500 (Internal Server Error) if the compte couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/comptes")
    @Timed
    public ResponseEntity<CompteFse> updateCompte(@RequestBody CompteFse compte) throws URISyntaxException {
        log.debug("REST request to update Compte : {}", compte);
        if (compte.getId() == null) {
            return createCompte(compte);
        }
        CompteFse result = compteService.save(compte);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, compte.getId().toString()))
                .body(result);
    }

    /**
     * GET /comptes : get all the comptes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of comptes
     * in body
     */
    @GetMapping("/comptes")
    @Timed
    public List<CompteFse> getAllComptes() {
        log.debug("REST request to get all Comptes");
        return compteService.findAll();
    }

    @PostMapping("/comptesfse/fournisseur")
    @Timed
    public HashMap<String, String> getAllFseComptes(
            @RequestParam("codefse") String codefse,
            @RequestParam("etiquette") String etiquette
    ) {
        CompteFse cpt = compteService.findCompteByEtiquetteAndFse(codefse, etiquette);
        HashMap<String, String> cptMap = new HashMap<String, String>();
        if (cpt != null) {

            cptMap.put("etiquette", etiquette);
            cptMap.put("compteBanque", cpt.getCodeBanque());
            cptMap.put("codeAgence", cpt.getCodeAgence());
            cptMap.put("codeFse", codefse);
            cptMap.put("numeroCompte", cpt.getNumeroCompte());
            cptMap.put("cle", cpt.getCle());
        } else {
            return null;
        }
        return cptMap;

    }

    /**
     * GET /comptes/:id : get the "id" compte.
     *
     * @param id the id of the compte to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the compte,
     * or with status 404 (Not Found)
     */
    @GetMapping("/comptes/{id}")
    @Timed
    public ResponseEntity<CompteFse> getCompte(@PathVariable Long id) {
        log.debug("REST request to get Compte : {}", id);
        CompteFse compte = compteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(compte));
    }

    /**
     * DELETE /comptes/:id : delete the "id" compte.
     *
     * @param id the id of the compte to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/comptes/{id}")
    @Timed
    public ResponseEntity<Void> deleteCompte(@PathVariable Long id) {
        log.debug("REST request to delete Compte : {}", id);
        compteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
