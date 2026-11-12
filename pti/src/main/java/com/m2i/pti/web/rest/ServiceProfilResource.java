package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.ServiceProfil;
import com.m2i.pti.domain.enumeration.TypeProfilService;
import com.m2i.pti.service.ServiceProfilService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.ServiceProfilDTO;
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
 * REST controller for managing ServiceProfil.
 */
@RestController
@RequestMapping("/api")
public class ServiceProfilResource {

    private final Logger log = LoggerFactory.getLogger(ServiceProfilResource.class);

    private static final String ENTITY_NAME = "serviceProfil";

    private final ServiceProfilService serviceProfilService;

    public ServiceProfilResource(ServiceProfilService serviceProfilService) {
        this.serviceProfilService = serviceProfilService;
    }

    /**
     * POST  /service-profils : Create a new serviceProfil.
     *
     * @param serviceProfilDTO the serviceProfilDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new serviceProfilDTO, or with status 400 (Bad Request) if the serviceProfil has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/service-profils")
    @Timed
    public ResponseEntity<ServiceProfilDTO> createServiceProfil(@Valid @RequestBody ServiceProfilDTO serviceProfilDTO) throws URISyntaxException {
        log.debug("REST request to save ServiceProfil : {}", serviceProfilDTO);
        if (serviceProfilDTO.getId() != null) {
            throw new BadRequestAlertException("A new serviceProfil cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ServiceProfilDTO result = serviceProfilService.save(serviceProfilDTO);
        return ResponseEntity.created(new URI("/api/service-profils/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /service-profils : Updates an existing serviceProfil.
     *
     * @param serviceProfilDTO the serviceProfilDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated serviceProfilDTO,
     * or with status 400 (Bad Request) if the serviceProfilDTO is not valid,
     * or with status 500 (Internal Server Error) if the serviceProfilDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/service-profils")
    @Timed
    public ResponseEntity<ServiceProfilDTO> updateServiceProfil(@Valid @RequestBody ServiceProfilDTO serviceProfilDTO) throws URISyntaxException {
        log.debug("REST request to update ServiceProfil : {}", serviceProfilDTO);
        if (serviceProfilDTO.getId() == null) {
            return createServiceProfil(serviceProfilDTO);
        }
        ServiceProfilDTO result = serviceProfilService.save(serviceProfilDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, serviceProfilDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /service-profils : get all the serviceProfils.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of serviceProfils in body
     */
    @GetMapping("/service-profils")
    @Timed
    public ResponseEntity<List<ServiceProfilDTO>> getAllServiceProfils(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of ServiceProfils");
        Page<ServiceProfilDTO> page = serviceProfilService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/service-profils");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    @GetMapping("/service-profils/byType/{type}")
    @Timed
    public List<ServiceProfilDTO> getAllServiceProfilsByType(@PathVariable TypeProfilService type){
        return serviceProfilService.findByType(type);
    }

    /**
     * GET  /service-profils/:id : get the "id" serviceProfil.
     *
     * @param id the id of the serviceProfilDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the serviceProfilDTO, or with status 404 (Not Found)
     */
    @GetMapping("/service-profils/{id}")
    @Timed
    public ResponseEntity<ServiceProfilDTO> getServiceProfil(@PathVariable Long id) {
        log.debug("REST request to get ServiceProfil : {}", id);
        ServiceProfilDTO serviceProfilDTO = serviceProfilService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(serviceProfilDTO));
    }

    /**
     * DELETE  /service-profils/:id : delete the "id" serviceProfil.
     *
     * @param id the id of the serviceProfilDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/service-profils/{id}")
    @Timed
    public ResponseEntity<Void> deleteServiceProfil(@PathVariable Long id) {
        log.debug("REST request to delete ServiceProfil : {}", id);
        serviceProfilService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
