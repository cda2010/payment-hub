package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.presentation.domain.ApplicationApi;
import com.m2i.omnicanal.presentation.service.ApiService;
import com.m2i.omnicanal.presentation.web.rest.util.HeaderUtil;

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
 * REST controller for managing Api.
 */
@RestController
@RequestMapping("/api")
public class ApiResource {

    private final Logger log = LoggerFactory.getLogger(ApiResource.class);

    private static final String ENTITY_NAME = "api";
        
    private final ApiService apiService;
    public ApiResource(ApiService apiService) {
        this.apiService = apiService;
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
        log.debug("REST request to save Api : {}", api);
        if (api.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new api cannot already have an ID")).body(null);
        }
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
    public List<ApplicationApi> getAllApis() {
        log.debug("REST request to get all Apis");
        return apiService.findAll();
    }

    /**
     * GET  /apis/:id : get the "id" api.
     *
     * @param id the id of the api to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the api, or with status 404 (Not Found)
     */
    @GetMapping("/apis/{id}")
    @Timed
    public ResponseEntity<ApplicationApi> getApi(@PathVariable Long id) {
        log.debug("REST request to get Api : {}", id);
        ApplicationApi api = apiService.findOne(id);
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

}
