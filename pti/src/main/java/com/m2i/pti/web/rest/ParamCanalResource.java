package com.m2i.pti.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

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
import com.m2i.pti.service.ParamCanalService;
import com.m2i.pti.service.dto.ParamCanalDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing Canal.
 */
@RestController
@RequestMapping("/api")
public class ParamCanalResource {

    private final Logger log = LoggerFactory.getLogger(ParamCanalResource.class);

    private static final String ENTITY_NAME = "param_canal";

    private final ParamCanalService paramCanalService;

    public ParamCanalResource(ParamCanalService paramCanalService) {
        this.paramCanalService = paramCanalService;
    }

    /**
     * POST /canals : Create a new canal.
     *
     * @param canalDTO the canalDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new canalDTO, or with status 400 (Bad Request) if the canal has already
     * an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/param-canal")
    @Timed
    public ResponseEntity<ParamCanalDTO> createParamCanal(@Valid @RequestBody ParamCanalDTO paramCanalDTO) throws URISyntaxException {
        log.debug("REST request to save Canal Application: {}", paramCanalDTO);
        Long poids = 0L;
        if (paramCanalDTO.getId() != null) {
            throw new BadRequestAlertException("A new canal cannot already have an ID", ENTITY_NAME, "idexists");
        }
        if (paramCanalDTO.getApplication() != null) {
            poids = poids + 1L;
        }
        if (paramCanalDTO.getTypeService() != null) {
            poids = poids + 2L;
        }
        if (paramCanalDTO.getUniteOrganisation() != null) {
            poids = poids + 3L;
        }
        paramCanalDTO.setPoids(poids);
        ParamCanalDTO result = paramCanalService.save(paramCanalDTO);
        return ResponseEntity.created(new URI("/api/param-canal/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @GetMapping("/param-canal")
    @Timed
    public ResponseEntity<List<ParamCanalDTO>> getAllParamCanalApplication(Pageable pageable) {
        Page<ParamCanalDTO> page = paramCanalService.getAllParamCanal(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/param-canal");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/param-canal-by-statut")
    @Timed
    public ResponseEntity<List<ParamCanalDTO>> getAllCanalApplicationByCanal(@RequestParam(required = true) String typeCanal, @RequestParam(required = true) String application, Pageable pageable) {
        Page<ParamCanalDTO> page = paramCanalService.getAllParamCanalByCanal(typeCanal, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/param-canal-by-statut");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/param-canal/{id}")
    @Timed
    public ResponseEntity<ParamCanalDTO> getParamCanal(@PathVariable Long id) {
        ParamCanalDTO paramCanalDTO = paramCanalService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(paramCanalDTO));
    }

    @DeleteMapping("/param-canal/{id}")
    @Timed
    public ResponseEntity<Void> deleteParamCanal(@PathVariable Long id) {
        paramCanalService.delete(id);
        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
                .build();
    }

    @PutMapping("/param-canal")
    @Timed
    public ResponseEntity<ParamCanalDTO> updateParamCanal(
            @Valid @RequestBody ParamCanalDTO paramCanalDTO
    )
            throws URISyntaxException {
        if (paramCanalDTO.getId() == null) {
            return createParamCanal(paramCanalDTO);
        }
        ParamCanalDTO result = paramCanalService.save(paramCanalDTO);
        return ResponseEntity
                .ok()
                .headers(
                        HeaderUtil.createEntityUpdateAlert(
                                ENTITY_NAME,
                                paramCanalDTO.getId().toString()
                        )
                )
                .body(result);
    }

}
