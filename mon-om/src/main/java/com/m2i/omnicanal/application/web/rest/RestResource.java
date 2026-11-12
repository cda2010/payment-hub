package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.ApplicationApi;
import com.m2i.omnicanal.application.domain.RestUser;
import com.m2i.omnicanal.application.service.ApiService;
import com.m2i.omnicanal.application.service.RestService;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;

import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

/**
 * REST controller for managing Api.
 */
@RestController
@RequestMapping("/api")
public class RestResource {

    private final Logger log = LoggerFactory.getLogger(RestResource.class);

    private static final String ENTITY_NAME = "rest";
        
    private final RestService restService;
     private final ApiService apiService;
    public RestResource(RestService restService,ApiService apiService) {
        this.restService = restService;
       this.apiService = apiService;
    }

    /**
     * POST  /apis : Create a new api.
     *
     * @param api the api to create
     * @return the ResponseEntity with status 201 (Created) and with body the new api, or with status 400 (Bad Request) if the api has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/rests")
    @Timed
    public ResponseEntity<RestUser> createApi(@RequestBody RestUser api) throws URISyntaxException {
        log.debug("REST request to save Api : {}", api);
        if (api.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new api cannot already have an ID")).body(null);
        }
  Integer maxCodeApi = apiService.findMaxCodeApi();
         log.debug("REST max Api : {}", maxCodeApi);
        String strCodeApi =  StringUtils.leftPad( maxCodeApi.toString(), 4, "0");
        api.setCodeApi(strCodeApi);
    
        RestUser result = restService.save(api);
        return ResponseEntity.created(new URI("/api/rests/" + result.getId()))
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
    @PutMapping("/rests")
    @Timed
    public ResponseEntity<RestUser> updateApi(@RequestBody RestUser api) throws URISyntaxException {
        log.debug("REST request to update Api : {}", api);
        if (api.getId() == null) {
            return (ResponseEntity<RestUser>) createApi(api);
        }
        RestUser result = restService.save(api);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, api.getId().toString()))
            .body(result);
    }

    /**
     * GET  /apis : get all the apis.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of apis in body
     */
    @GetMapping("/rests")
    @Timed
    public List<RestUser> getAllApis() {
        log.debug("REST request to get all Apis");
        return restService.findAll();
    }

    /**
     * GET  /apis/:id : get the "id" api.
     *
     * @param id the id of the api to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the api, or with status 404 (Not Found)
     */
    @GetMapping("/rests/{id}")
    @Timed
    public ResponseEntity<RestUser> getApi(@PathVariable Long id) {
        log.debug("REST request to get Api : {}", id);
        RestUser api = restService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(api));
    }

    /**
     * DELETE  /apis/:id : delete the "id" api.
     *
     * @param id the id of the api to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/rests/{id}")
    @Timed
    public ResponseEntity<Void> deleteApi(@PathVariable Long id) {
        log.debug("REST request to  Api : {}", id);
        restService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
