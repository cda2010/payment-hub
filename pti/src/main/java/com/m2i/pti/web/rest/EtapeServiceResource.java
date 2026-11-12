package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.EtapeService;
import com.m2i.pti.domain.enumeration.ActionService;
import com.m2i.pti.domain.enumeration.TypeNotification;
import com.m2i.pti.repository.EtapeServiceRepository;
import com.m2i.pti.service.EtapeServiceService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.EtapeServiceDTO;
import com.m2i.pti.service.mapper.EtapeServiceMapper;
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

import java.net.URI;
import java.net.URISyntaxException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Notification.
 */
@RestController
@RequestMapping("/api")
public class EtapeServiceResource {

    private final Logger log = LoggerFactory.getLogger(EtapeServiceResource.class);

    private static final String ENTITY_NAME = "etapeService";

    private final EtapeServiceService etapeServiceService;

    private final EtapeServiceRepository etapeServiceRepository;

    private final EtapeServiceMapper etapeServiceMapper;

    public EtapeServiceResource(EtapeServiceService etapeServiceService,EtapeServiceRepository etapeServiceRepository,EtapeServiceMapper etapeServiceMapper) {
        this.etapeServiceService = etapeServiceService;
        this.etapeServiceRepository = etapeServiceRepository;
        this.etapeServiceMapper = etapeServiceMapper;
    }

    /**
     * POST  /etapeService : Create a new notification.
     *
     * @param etapeServiceDTO the etapeServiceDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new etapeServiceDTO, or with status 400 (Bad Request) if the notification has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/etapeService")
    @Timed
    public ResponseEntity<EtapeServiceDTO> createEtapeService(@RequestBody EtapeServiceDTO etapeServiceDTO) throws URISyntaxException {  
        log.debug("REST request to save EtapeServiceService : {}", etapeServiceDTO);
        if (etapeServiceDTO.getId() != null) {
            throw new BadRequestAlertException("A new etape service cannot already have an ID", ENTITY_NAME, "idexists");
        }  
        EtapeServiceDTO result = etapeServiceService.save(etapeServiceDTO); 
        System.out.println("00000000000000000000000000000000000000"+result.getId());
        return ResponseEntity.created(new URI("/api/etapeService/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /etapeService : Updates an existing notification.
     * 
     * @param etapeServiceDTO the etapeServiceDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated etapeServiceDTO,
     * or with status 400 (Bad Request) if the etapeServiceDTO is not valid,
     * or with status 500 (Internal Server Error) if the etapeServiceDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/etapeService")
    @Timed
    public ResponseEntity<EtapeServiceDTO> updateEtapeService(@RequestBody EtapeServiceDTO etapeServiceDTO) throws URISyntaxException {
        log.debug("REST request to update Notification : {}", etapeServiceDTO);
        if (etapeServiceDTO.getId() == null) {
            return createEtapeService(etapeServiceDTO);
        }
        EtapeServiceDTO result = etapeServiceService.save(etapeServiceDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, etapeServiceDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /etapeService : get all the etapeService.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of etapeService in body
     */
    @GetMapping("/etapeService")
    @Timed
    public ResponseEntity<List<EtapeServiceDTO>> getAllEtapeService( @RequestParam(required = false) ActionService actionService, Pageable pageable) {
        log.debug("REST request to get a page of Notifications"+actionService);
        Page<EtapeServiceDTO> page = etapeServiceService.findByCriteria(actionService,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/etapeService");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK); 
    }

    /** 
     * GET  /etapeService/:id : get the "id" notification.
     *
     * @param id the id of the etapeServiceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the etapeServiceDTO, or with status 404 (Not Found)
     */
   /*  
    @GetMapping("/etapeService/{id}")
    @Timed
    public ResponseEntity<EtapeServiceDTO> getEtapeService(@PathVariable Long id) {
        log.debug("REST request to get Notification : {}", id);
        EtapeService etapeService = etapeServiceRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(etapeServiceMapper.toDto(etapeService)));
    } */

    /**
     * DELETE  /etapeService/:id : delete the "id" notification.
     *
     * @param id the id of the etapeServiceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/etapeService/{id}")
    @Timed
    public ResponseEntity<Void> deleteEtapeService(@PathVariable Long id) {
        etapeServiceRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
