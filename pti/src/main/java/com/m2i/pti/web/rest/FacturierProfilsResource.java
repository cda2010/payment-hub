package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.repository.FacturierProfilRepository;
import com.m2i.pti.service.FacturierProfilService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.FacturierProfilDTO;
import com.m2i.pti.service.mapper.EtapeServiceMapper;
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

/**
 * REST controller for managing Notification.
 */
@RestController
@RequestMapping("/api")
public class FacturierProfilsResource {

    private final Logger log = LoggerFactory.getLogger(FacturierProfilsResource.class);

    private static final String ENTITY_NAME = "facturierProfils";

    private final FacturierProfilService facturierProfilService;

    private final FacturierProfilRepository facturierProfilRepository;


    public FacturierProfilsResource(FacturierProfilService facturierProfilService,FacturierProfilRepository facturierProfilRepository) {
        this.facturierProfilService = facturierProfilService;
        this.facturierProfilRepository = facturierProfilRepository;
    }

    /**
     * POST  /etapeService : Create a new notification.
     *
     * @param etapeServiceDTO the etapeServiceDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new etapeServiceDTO, or with status 400 (Bad Request) if the notification has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/facturierProfil")
    @Timed
    public ResponseEntity<FacturierProfilDTO> createFacturierProfil(@RequestBody FacturierProfilDTO facturierProfilDTO) throws URISyntaxException {  
        log.debug("REST request to save EtapeServiceService : {}", facturierProfilDTO);
        if (facturierProfilDTO.getId() != null) {
            throw new BadRequestAlertException("A new etape service cannot already have an ID", ENTITY_NAME, "idexists");
        }  
        FacturierProfilDTO result = facturierProfilService.save(facturierProfilDTO);
        log.debug("resultat value", result);
        return ResponseEntity.created(new URI("/api/facturierProfil/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    } 

    /**
     * PUT  /etapeService : Updates an existing notification.
     * 
     */
    @PutMapping("/facturierProfil")
    @Timed
    public ResponseEntity<FacturierProfilDTO> updateEtapeService(@RequestBody FacturierProfilDTO facturierProfilDTO) throws URISyntaxException {
        log.debug("REST request to update Notification : {}", facturierProfilDTO);
        if (facturierProfilDTO.getId() == null) {
            return createFacturierProfil(facturierProfilDTO);
        }
        FacturierProfilDTO result = facturierProfilService.save(facturierProfilDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, facturierProfilDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /etapeService : get all the etapeService.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of etapeService in body
     */
    @GetMapping("/facturierProfil")
    @Timed
    public ResponseEntity<List<FacturierProfilDTO>> getAllFacturierProfil(Pageable pageable) {
        Page<FacturierProfilDTO> page = facturierProfilService.findByFacturierProfilsAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/facturierProfil");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK); 
    }

    /**
     * GET  /etapeService/:id : get the "id" notification.
     *
     * @param id the id of the etapeServiceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the etapeServiceDTO, or with status 404 (Not Found)
     */
   /*  
    

    /**
     * DELETE  /etapeService/:id : delete the "id" notification.
     *
     * @param id the id of the etapeServiceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/facturierProfil/{id}")
    @Timed
    public ResponseEntity<Void> deleteEtapeService(@PathVariable Long id) {
        log.debug("REST request to delete Notification : {}", id);
        facturierProfilRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
