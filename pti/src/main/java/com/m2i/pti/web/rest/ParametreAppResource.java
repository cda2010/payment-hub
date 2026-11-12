package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.ParametreApp;
import com.m2i.pti.domain.UniteOrganisation;

import com.m2i.pti.domain.enumeration.TypeParametre;
import com.m2i.pti.repository.ParametreAppRepository;
import com.m2i.pti.repository.TransactionComptePtiRepository;
import com.m2i.pti.service.dto.ParametreAppDTO;
import com.m2i.pti.service.mapper.ParametreAppMapper;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
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
 * REST controller for managing ParametreApp.
 */
@RestController
@RequestMapping("/api")
public class ParametreAppResource {

    private final Logger log = LoggerFactory.getLogger(ParametreAppResource.class);

    private static final String ENTITY_NAME = "parametreApp";

    private final ParametreAppRepository parametreAppRepository;
    private final TransactionComptePtiRepository transactionComptePtiRepository;

    private final ParametreAppMapper parametreAppMapper;
    public ParametreAppResource(ParametreAppRepository parametreAppRepository, ParametreAppMapper parametreAppMapper,TransactionComptePtiRepository transactionComptePtiRepository) {
        this.parametreAppRepository = parametreAppRepository;
        this.parametreAppMapper = parametreAppMapper;
        this.transactionComptePtiRepository=transactionComptePtiRepository;
    }

    /**
     * POST  /parametre-apps : Create a new parametreApp.
     *
     * @param parametreApp the parametreApp to create
     * @return the ResponseEntity with status 201 (Created) and with body the new parametreApp, or with status 400 (Bad Request) if the parametreApp has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/parametre-apps")
    @Timed
    public ResponseEntity<ParametreApp> createParametreApp(@RequestBody ParametreApp parametreApp) throws URISyntaxException {
        log.debug("REST request to save ParametreApp : {}", parametreApp);
        if (parametreApp.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new parametreApp cannot already have an ID")).body(null);
        }
        ParametreApp result = parametreAppRepository.save(parametreApp);
        return ResponseEntity.created(new URI("/api/parametre-apps/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /parametre-apps : Updates an existing parametreApp.
     *
     * @param parametreApp the parametreApp to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated parametreApp,
     * or with status 400 (Bad Request) if the parametreApp is not valid,
     * or with status 500 (Internal Server Error) if the parametreApp couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/parametre-apps")
    @Timed
    public ResponseEntity<ParametreApp> updateParametreApp(@RequestBody ParametreApp parametreApp) throws URISyntaxException {
        log.debug("REST request to update ParametreApp : {}", parametreApp);
        if (parametreApp.getId() == null) {
            return createParametreApp(parametreApp);
        }
        ParametreApp result = parametreAppRepository.save(parametreApp);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, parametreApp.getId().toString()))
            .body(result);
    }

    /**
     * GET  /parametre-apps : get all the parametreApps.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of parametreApps in body
     */
    @GetMapping("/parametre-apps")
    @Timed
    public ResponseEntity<List<ParametreAppDTO>> getAllParametreApps(@ApiParam Pageable pageable) {
        log.debug("REST request to get all ParametreApps");
        Page<ParametreAppDTO> page = parametreAppRepository.findAll(pageable).map(parametreAppMapper::toDto);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/parametre-app");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        //return parametreAppRepository.findAll();
    }


    @GetMapping("/parametre-apps/search")
    @Timed
    public ParametreApp getByTypeAndNom(@RequestParam() String nom, @RequestParam TypeParametre typeParametre) {
        log.debug("REST request to get all ParametreApps");
        return parametreAppRepository.findByTypeAndNom(typeParametre,nom);
    }

    @GetMapping("/parametre-apps/nom/{nom}/code/{codeFSE}")
    @Timed
    public ParametreApp getByNomAndCodeFSE(@PathVariable String nom,@PathVariable String codeFSE) {
        return parametreAppRepository.findByTypeAndNomAndCodeFse(nom,codeFSE);
    }
    @GetMapping("/parametre-apps/isCheckBanking/{codeFSE}")
    @Timed
    public ParametreApp getVerifCodeFromCoreBanking(@PathVariable String codeFSE) {
        
         ParametreApp fournisseur = parametreAppRepository.findByTypeAndNomAndCodeFse("VERIFIER_EVT_COREBANKING", codeFSE);
         
        return fournisseur;
    }
    /**
     * GET  /parametre-apps/:id : get the "id" parametreApp.
     *
     * @param id the id of the parametreApp to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the parametreApp, or with status 404 (Not Found)
     */
    @GetMapping("/parametre-apps/{id}")
    @Timed
    public ResponseEntity<ParametreApp> getParametreApp(@PathVariable Long id) {
        log.debug("REST request to get ParametreApp : {}", id);
        ParametreApp parametreApp = parametreAppRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(parametreApp));
    }

    /**
     * DELETE  /parametre-apps/:id : delete the "id" parametreApp.
     *
     * @param id the id of the parametreApp to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/parametre-apps/{id}")
    @Timed
    public ResponseEntity<Void> deleteParametreApp(@PathVariable Long id) {
        log.debug("REST request to delete ParametreApp : {}", id);
        parametreAppRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    private String getIfVerifCodeFromCoreBanking(String codeFse){
         ParametreApp fournisseur = parametreAppRepository.findByTypeAndNomAndCodeFse("VERIFIER_EVT_COREBANKING", codeFse);
         if (fournisseur!=null){
              return fournisseur.getValeur();
         }
        return null;
    }
    @GetMapping("/found-parametre")
    @Timed
    public ParametreApp foundParametre(@RequestParam(required =true) Long pereUoId){
        String codeFse = null;
       UniteOrganisation uo = transactionComptePtiRepository.findByUoId(pereUoId);
        if (uo != null) {
            codeFse = uo.getCodeFse();
        }
        ParametreApp devise = parametreAppRepository.findByTypeAndNomAndCodeFse("DEVISE", codeFse);

        if (devise == null) {
            return new ParametreApp();
        }
       return devise;
    }
}
