/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.repository.TypeServiceRepository;
import com.m2i.omnicanal.application.service.TypeServiceService;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;
import com.m2i.omnicanal.application.web.rest.util.PaginationUtil;
import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.model.TypeService;
import io.github.jhipster.web.util.ResponseUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author cda
 */
@RestController
@RequestMapping("/api")
public class TypeServiceRessource {
    private final Logger log = LoggerFactory.getLogger(TypeServiceRessource.class);
    private final TypeServiceService typeServiceService;
    private final TypeServiceRepository typeServiceRepository;
  private static final String ENTITY_NAME = "typeService";
    public TypeServiceRessource(TypeServiceService typeServiceService, TypeServiceRepository typeServiceRepository) {
        this.typeServiceService = typeServiceService;
        this.typeServiceRepository = typeServiceRepository;
    }
        @GetMapping("/type-services")
    @Timed
//    public List<TypeService> getAllTypeService() {
//        log.debug("REST request to get all ServiceMethodes");
//        return typeServiceService.findAll();
//    }
    public ResponseEntity<List<TypeService>> getAllTypeService(Pageable pageable) {
        log.debug("REST request to get all ServiceMethodes");
            Page<TypeService> page = typeServiceRepository.findAll(pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/type-services");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);

    }
    @PostMapping("/type-services")
    @Timed
    public ResponseEntity<TypeService> createTypeService(@RequestBody TypeService serv) throws URISyntaxException {
        log.debug("REST request to save TypeServ : {}", serv);
        if (serv.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new service cannot already have an ID")).body(null);
        }
        TypeService result = typeServiceService.save(serv);
        return ResponseEntity.created(new URI("/api/type-service/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    @PutMapping("/type-services")
    @Timed
    public ResponseEntity<TypeService> updateTypeService(@RequestBody TypeService serv) throws URISyntaxException {
        log.debug("REST request to update TypeServ : {}", serv);
        if (serv.getId() == null) {
            return createTypeService(serv);
        }
        TypeService result = typeServiceService.save(serv);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, serv.getId().toString()))
            .body(result);
    }
    @GetMapping("/type-services/{id}")
    @Timed
    public ResponseEntity<TypeService> getTypeServ(@PathVariable Long id) {
        log.debug("REST request to get TypeServ : {}", id);
        TypeService tservice = typeServiceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(tservice));
    }

  @DeleteMapping("/type-services/{id}")
    @Timed
    public ResponseEntity<Void> deleteTypeServ(@PathVariable Long id) {
        log.debug("REST request to delete TypeServ : {}", id);
        typeServiceService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
