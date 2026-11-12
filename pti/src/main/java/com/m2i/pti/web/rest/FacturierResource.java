package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.CanalCommunication;
import com.m2i.pti.domain.Facturier;
import com.m2i.pti.domain.Ftp;
import com.m2i.pti.domain.RecepteurEmail;
import com.m2i.pti.repository.CanalCommunicationRepository;
import com.m2i.pti.repository.FacturierRepository;
import com.m2i.pti.repository.FtpRepository;
import com.m2i.pti.repository.RecepteurEmailRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.FacturierService;
import com.m2i.pti.service.dto.FacturierDTO;
import com.m2i.pti.service.dto.FtpDTO;
import com.m2i.pti.service.dto.RecepteurEmailDTO;
import com.m2i.pti.service.mapper.FacturierMapper;
import com.m2i.pti.service.mapper.FtpMapper;
import com.m2i.pti.service.mapper.RecepteurEmailMapper;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Facturier.
 */
@RestController
@RequestMapping("/api")
public class FacturierResource {

    private final Logger log = LoggerFactory.getLogger(FacturierResource.class);

    private static final String ENTITY_NAME = "facturier";

    private final FacturierRepository facturierRepository;
    private final FacturierService facturierService;
    private final FacturierMapper facturierMapper;
    private final RecepteurEmailMapper recepteurEmailMapper;
    private final FtpMapper ftpMapper;
    private final UniteOrganisationRepository uniteOrganisationRepository;
    private final RecepteurEmailRepository recepteurEmailRepository;
    private final FtpRepository ftpRepository;
    private final CanalCommunicationRepository canalCommunicationrRepository;

    public FacturierResource(FacturierRepository facturierRepository, FacturierService facturierService, UniteOrganisationRepository uniteOrganisationRepository,
        CanalCommunicationRepository canalCommunicationrRepository,RecepteurEmailRepository recepteurEmailRepository,FtpRepository ftpRepository,FacturierMapper facturierMapper,
        RecepteurEmailMapper recepteurEmailMapper,FtpMapper ftpMapper) {
        this.facturierRepository = facturierRepository;
        this.facturierService = facturierService;
        this.facturierMapper = facturierMapper;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.canalCommunicationrRepository=canalCommunicationrRepository;
        this.recepteurEmailRepository=recepteurEmailRepository;
        this.ftpRepository=ftpRepository;
        this.recepteurEmailMapper=recepteurEmailMapper;
        this.ftpMapper=ftpMapper;
    }

    /**
     * POST  /facturiers : Create a new facturier.
     *
     * @param facturier the facturier to create
     * @return the ResponseEntity with status 201 (Created) and with body the new facturier, or with status 400 (Bad Request) if the facturier has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/facturiers")
    @Timed
    @Transactional
    public ResponseEntity<Facturier> createFacturier(@RequestBody FacturierDTO facturierDTO) throws URISyntaxException {
        log.debug("REST request to save Facturier : {}", facturierDTO);
        if (facturierDTO.getId() != null) {
            throw new BadRequestAlertException("A new facturier cannot already have an ID", ENTITY_NAME, "idexists");
        }

        Facturier result = facturierService.save(facturierDTO);

       /*  Facturier result = facturierRepository.save(facturier);
        if(result.getUniteOrganisation()!=null){
            result.setNomFacturier(uniteOrganisationRepository.findById(result.getUniteOrganisation().getId()).orElse(null).getLibelleUniteOrganisation());
            result = facturierRepository.save(result);
        }
        return ResponseEntity.created(new URI("/api/facturiers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result); */
            return ResponseEntity.created(new URI("/api/facturiers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /facturiers : Updates an existing facturier.
     *
     * @param facturier the facturier to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated facturier,
     * or with status 400 (Bad Request) if the facturier is not valid,
     * or with status 500 (Internal Server Error) if the facturier couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
   /* fonction originale 
   @PutMapping("/facturiers")
    @Timed
    public ResponseEntity<Facturier> updateFacturier(@RequestBody FacturierDTO facturierDTO) throws URISyntaxException {
        log.debug("REST request to update Facturier : {}", facturierDTO);
        if (facturierDTO.getId() == null) {
            return createFacturier(facturierDTO);
        }
         if(1==1)
            throw new CustomParameterizedException("Donnnees non recues");
        Facturier result = facturierService.save(facturierDTO);
        if(result.getUniteOrganisation()!=null){
            result.setNomFacturier(uniteOrganisationRepository.findById(result.getUniteOrganisation().getId()).orElse(null).getLibelleUniteOrganisation());
            result = facturierRepository.save(result);
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, facturierDTO.getId().toString()))
            .body(result);
    } */

    @PutMapping("/facturiers")
    @Timed
    public ResponseEntity<Facturier> updateFacturier(@RequestBody FacturierDTO facturierDTO) throws URISyntaxException {
        log.debug("REST request to update Facturier : {}", facturierDTO);
        if (facturierDTO.getId() == null) {
            return createFacturier(facturierDTO);
        }

        Facturier result = facturierService.update(facturierDTO);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, facturierDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /facturiers : get all the facturiers.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of facturiers in body
     */
/*    @GetMapping("/facturiers")
    @Timed
    public List<Facturier> getAllFacturiers() {
        log.debug("REST request to get all Facturiers");
        return facturierRepository.findAll();
        }*/
    /**
     * GET  /facturiers/:id : get the "id" facturier.
     *
     * @param id the id of the facturier to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the facturier, or with status 404 (Not Found)
     */
    @GetMapping("/facturiers")
    @Timed
    public ResponseEntity<List<Facturier>> getAllFacturiersPage(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of Facturiers");
        Page<Facturier> page = facturierService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/facturier");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    /* @GetMapping("/facturiers/{id}")
    @Timed
    public ResponseEntity<Facturier> getFacturier(@PathVariable Long id) {
        log.debug("REST request to get Facturier : {}", id);
        Facturier facturier = facturierRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(facturier));
    } */

    @GetMapping("/facturiers/{id}")
    @Timed
    public ResponseEntity<FacturierDTO> getFacturierDTO(@PathVariable Long id) {
        log.debug("REST request to get Facturier : {}", id);
        Facturier facturier = facturierRepository.findById(id).orElse(null);
        FacturierDTO facturierDTO = facturierMapper.toDto(facturier);

        Long idRecepteurEmail = canalCommunicationrRepository.getIdRecepteurEmailByIdFacturier(id);
        if(idRecepteurEmail!=null){
            RecepteurEmail recepteurEmail = recepteurEmailRepository.findById(idRecepteurEmail).orElse(null);
            RecepteurEmailDTO recepteurEmailDTO = recepteurEmailMapper.toDto(recepteurEmail);
            facturierDTO.setRecepteurEmailDTO(recepteurEmailDTO);
        }

        Long idFtp = canalCommunicationrRepository.getIdFtpByIdFacturier(id);
        if(idFtp!=null){
            Ftp ftp = ftpRepository.findById(idFtp).orElse(null);
            FtpDTO ftpDTO = ftpMapper.toDto(ftp);
            facturierDTO.setFtpDTO(ftpDTO);
        }

        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(facturierDTO));
    }

    @GetMapping("/facturiers/of-classification/{id}")
    @Timed
    public List<FacturierDTO> getFacturierOfClassification(@PathVariable Long id) {
        List<Facturier> l=facturierRepository.findAllByClassificationId(id);
        int longueur=l.size();
        int i;
        List<FacturierDTO> retour=new ArrayList<FacturierDTO>();
        for( i=0;i<longueur; i++){
            retour.add(new FacturierDTO(l.get(i)));
        }
        return  retour;
    }

    /**
     * DELETE  /facturiers/:id : delete the "id" facturier.
     *
     * @param id the id of the facturier to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/facturiers/{id}")
    @Timed
    public ResponseEntity<Void> deleteFacturier(@PathVariable Long id) {
        log.debug("REST request to delete Facturier : {}", id);
        facturierRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/facturiers-optimiser")
    @Timed
    ResponseEntity<List<FacturierDTO>> getoptimiseFacturier(@ApiParam Pageable pageable){
        log.debug("REST request to get a page of Facturiers");
        Page<FacturierDTO> page = facturierService.findAllOptimiser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/facturier");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
