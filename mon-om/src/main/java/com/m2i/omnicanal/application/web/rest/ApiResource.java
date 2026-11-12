package com.m2i.omnicanal.application.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.ApplicationApi;
import com.m2i.omnicanal.application.repository.ApiRepository;
import com.m2i.omnicanal.application.service.ApiService;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;
import com.m2i.omnicanal.application.web.rest.util.PaginationUtil;
import com.m2i.omnicanal.model.Api;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing Api.
 */
@RestController
@RequestMapping("/api")
public class ApiResource {

    private final Logger log = LoggerFactory.getLogger(ApiResource.class);

    private static final String ENTITY_NAME = "api";

    private final ApiService apiService;

    private final ApiRepository apiRepository;

    public ApiResource(ApiService apiService, ApiRepository apiRepository) {
        this.apiService = apiService;

        this.apiRepository = apiRepository;
    }

    /**
     * POST  /apis : Create a new api.
     *
     * @param api the api to create
     * @return the ResponseEntity with status 201 (Created) and with body the new api, or with status 400 (Bad Request) if the api has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */

    @PostMapping("/apis")
    @Timed
    public ResponseEntity<ApplicationApi> createApi(@RequestBody ApplicationApi api) throws URISyntaxException {
        log.debug("REST requestXXXXXXXXXX to save Api : {}", api);
        if (api.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new api cannot already have an ID")).body(null);
        }

        Integer maxCodeApi = apiService.findMaxCodeApi();
         log.debug("REST max Api : {}", maxCodeApi);
        String strCodeApi =  StringUtils.leftPad( maxCodeApi.toString(), 4, "0");
        api.setCodeApi(strCodeApi);
        ApplicationApi result = apiService.save(api);
        return ResponseEntity.created(new URI("/api/apis/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /apis : Updates an existing api.
     *
     * @param api the api to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated api,
     * or with status 400 (Bad Request) if the api is not valid,
     * or with status 500 (Internal Server Error) if the api couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/apis")
    @Timed
    public ResponseEntity<ApplicationApi> updateApi(@RequestBody ApplicationApi api) throws URISyntaxException {
        log.debug("REST request to update Api : {}", api);
        if (api.getId() == null) {
            return (ResponseEntity<ApplicationApi>) createApi(api);
        }
        ApplicationApi result = apiService.save(api);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, api.getId().toString()))
            .body(result);
    }

    /**
     * GET  /apis : get all the apis.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of apis in body
     */
    @GetMapping("/apis")
    @Timed
    public ResponseEntity<List<Api>> getAllApis(Pageable pageable,
        @RequestParam(required = false) String nomApi,
        @RequestParam(required = false) String fournisseur) {
        
        log.debug("REST request to get all Apis with filters nomApi: {} and fournisseur: {}", nomApi, fournisseur);
        
        Page<Api> page;
        if (StringUtils.isNotEmpty(nomApi) || StringUtils.isNotEmpty(fournisseur)) {
            page = apiRepository.findByFilters(nomApi, fournisseur, pageable);
        } else {
            page = apiRepository.findAll(pageable);
        }
        
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/apis");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /apis/:id : get the "id" api.
     *
     * @param id the id of the api to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the api, or with status 404 (Not Found)
     */
    @GetMapping("/apis/{id}")
    @Timed
    public ResponseEntity<Api> getApi(@PathVariable Long id) {
        log.debug("REST request to get Api : {}", id);
        Api api = apiService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(api));
    }

    /**
     * DELETE  /apis/:id : delete the "id" api.
     *
     * @param id the id of the api to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/apis/{id}")
    @Timed
    public ResponseEntity<Void> deleteApi(@PathVariable Long id) {
        log.debug("REST request to  Api : {}", id);
        apiService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    // Duplication de l'API avec ses methodes et ses parametres
    @PostMapping("/apis/{id}/duplicate")   
    @Timed
    public ResponseEntity<ApplicationApi> duplicateApi(@PathVariable Long id, @RequestBody ApplicationApi newApi) throws URISyntaxException {
        log.debug("REST request to duplicate Api : {}", id);
        
        // Recuperation de l'API avec toutes ses methodes et ses parametres
        Api originalApi = apiRepository.findOneWithMethodesAndParams(id);
        if (originalApi == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Creation de la nouvelle API
        newApi.setId(null);
        Integer maxCodeApi = apiService.findMaxCodeApi();
        String strCodeApi = StringUtils.leftPad(maxCodeApi.toString(), 4, "0");
        newApi.setCodeApi(strCodeApi);
        
        // Enregistrement de la nouvelle API et duplication de ses methodes et parametres
        ApplicationApi result = apiService.duplicateWithMethodsAndParams(originalApi, newApi);
        
        return ResponseEntity.created(new URI("/api/apis/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

}
