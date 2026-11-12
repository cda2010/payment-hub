package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.SommeSolde;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.service.mapper.UniteOrganisationMapper;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.service.dto.UniteOrganisationDTO;
import com.m2i.pti.web.rest.util.PaginationUtil;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing UniteOrganisation.
 */
@RestController
@RequestMapping("/api")
public class UniteOrganisationResource {

    private final Logger log = LoggerFactory.getLogger(UniteOrganisationResource.class);

    private static final String ENTITY_NAME = "uniteOrganisation";

    private final UniteOrganisationService uniteOrganisationService;

    private final UniteOrganisationRepository uniteOrganisationRepository;

    private final UniteOrganisationMapper uniteOrganisationMapper;


    public UniteOrganisationResource(UniteOrganisationService uniteOrganisationService, UniteOrganisationRepository uniteOrganisationRepository, UniteOrganisationMapper uniteOrganisationMapper) {
        this.uniteOrganisationService = uniteOrganisationService;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.uniteOrganisationMapper = uniteOrganisationMapper;
    }

    /**
     * POST  /unite-organisations : Create a new uniteOrganisation.
     *
     * @param uniteOrganisationDTO the uniteOrganisationDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new uniteOrganisationDTO, or with status 400 (Bad Request) if the uniteOrganisation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/unite-organisations")
    @Timed
    public ResponseEntity<UniteOrganisationDTO> createUniteOrganisation(@Valid @RequestBody UniteOrganisationDTO uniteOrganisationDTO) throws URISyntaxException {
        log.debug("REST request to save UniteOrganisation : {}", uniteOrganisationDTO);
        if (uniteOrganisationDTO.getId() != null) {
            throw new BadRequestAlertException("A new uniteOrganisation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        UniteOrganisationDTO result = uniteOrganisationService.saveNew(uniteOrganisationDTO);
        return ResponseEntity.created(new URI("/api/unite-organisations/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }



    /**
     * PUT  /unite-organisations : Updates an existing uniteOrganisation.
     *
     * @param uniteOrganisationDTO the uniteOrganisationDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated uniteOrganisationDTO,
     * or with status 400 (Bad Request) if the uniteOrganisationDTO is not valid,
     * or with status 500 (Internal Server Error) if the uniteOrganisationDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/unite-organisations")
    @Timed
    public ResponseEntity<UniteOrganisationDTO> updateUniteOrganisation(@Valid @RequestBody UniteOrganisationDTO uniteOrganisationDTO) throws URISyntaxException {
        log.debug("REST request to update UniteOrganisation : {}", uniteOrganisationDTO);
        if (uniteOrganisationDTO.getId() == null) {
            return createUniteOrganisation(uniteOrganisationDTO);
        }
        UniteOrganisationDTO result = uniteOrganisationService.saveNew(uniteOrganisationDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, uniteOrganisationDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /unite-organisations : get all the uniteOrganisations.
     *
     *
     * @return the ResponseEntity with status 200 (OK) and the list of uniteOrganisations in body
     */
    @GetMapping("/unite-organisations")
    @Timed
    public List<UniteOrganisationDTO> getAllUniteOrganisations( ) {
        log.debug("REST request to get a page of UniteOrganisations");
        List<UniteOrganisationDTO> list = uniteOrganisationService.findByUserConnect();
        return list;
    }
    @GetMapping("/unite-organisations-paginer")
    @Timed
    public ResponseEntity<List<UniteOrganisationDTO>> getAllUniteOrganisationsByPage(@RequestParam(required = false)String libelleUniteOrganisation,@RequestParam(required = false)String codeUO,@RequestParam(required = false)Integer numeroOrdre,@RequestParam(required = false)String typeUO,@RequestParam(required = false) Long pereUoId,@RequestParam(required = false) Long groupeId,@RequestParam(required = false) Long villeId,@RequestParam(required = false) String adresse, Pageable pageable) {
        log.debug("request to get UO with pereuo {}",pereUoId);
        if(pereUoId!=null){
            Page<UniteOrganisationDTO> page=uniteOrganisationService.findByPereUoId(libelleUniteOrganisation,codeUO,numeroOrdre,typeUO,pereUoId,groupeId,villeId,adresse,pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/unite-organisations-paginer");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        }
        Page<UniteOrganisationDTO> page=uniteOrganisationService.findAll(libelleUniteOrganisation,codeUO,numeroOrdre,typeUO,groupeId,villeId,adresse,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/unite-organisations-paginer");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    @GetMapping("/unite-organisations-paginer-op")
    @Timed
    public ResponseEntity<List<UniteOrganisationDTO>> getAllUniteOrganisationsByPageOp(
            @RequestParam(required = false)String libelleUniteOrganisation,
            @RequestParam(required = false)String codeUO,
            @RequestParam(required = false)Integer numeroOrdre,
            @RequestParam(required = false)String typeUO,
            @RequestParam(required = false) Long pereUoId,
            @RequestParam(required = false) Long groupeId,
            @RequestParam(required = false) Long villeId,
            @RequestParam(required = false) String adresse, Pageable pageable) {
        if(pereUoId!=null){
            Page<UniteOrganisationDTO> page=uniteOrganisationService.findByPereUoIdOp(libelleUniteOrganisation,codeUO,numeroOrdre,typeUO,pereUoId,groupeId,villeId,adresse,pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/unite-organisations-paginer");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        }
        Page<UniteOrganisationDTO> page=uniteOrganisationService.findAll(libelleUniteOrganisation,codeUO,numeroOrdre,typeUO,groupeId,villeId,adresse,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/unite-organisations-paginer");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/unite-organisations/{type}/typeUO")
    @Timed
    public List<UniteOrganisationDTO> getAllUniteOrganisationsByType(@PathVariable TypeUO type) {
        log.debug("REST request to get a page of UniteOrganisations");
        return uniteOrganisationService.findAllByTypeUo(type);
    }

    @GetMapping("/unite-organisations/{type}/typeUO-optimise")
    @Timed
    public List<UniteOrganisationDTO> getAllUniteOrganisationsByTypeOptimise(@PathVariable TypeUO type) {
        log.debug("REST request to get a page of UniteOrganisations");
        return uniteOrganisationService.findAllByTypeUoOptimise(type);
    }




    @GetMapping("/unite-organisations/uo_fils_by_type")
    @Timed
    public List<UniteOrganisationDTO> getAllUniteOrganisationsFilsByType(@RequestParam Long uoId,@RequestParam TypeUO type) {
        log.debug("REST request to get a page of UniteOrganisations");
        UniteOrganisation uo=uniteOrganisationRepository.findById(uoId).orElse(null);
        ArrayList<UniteOrganisation> listFils=new ArrayList<>();
        return uniteOrganisationService.findAllUoFilsByTypeFiltre(type,uo,listFils);
    }

    @GetMapping("/unite-organisations/uo_fils_by_type_optimiser")
    @Timed
    public List<UniteOrganisationDTO> getAllUniteOrganisationsFilsByTypeOptimiser(@RequestParam Long uoId,@RequestParam TypeUO type) {
        log.debug("REST request to get a page of UniteOrganisations");
        UniteOrganisation uo=uniteOrganisationRepository.findById(uoId).orElse(null);
        ArrayList<UniteOrganisationDTO> listFils=new ArrayList<>();
        return uniteOrganisationService.findAllUoFilsByTypeFiltreOptimiser(type,uo.getId(),listFils);
    }

    @GetMapping("/unite-organisations/uo_fils")
    @Timed
    public List<UniteOrganisationDTO> getAllUniteOrganisationsFils(@RequestParam Long uoId) {
        log.debug("REST request to get a page of UniteOrganisations");
        UniteOrganisation uo=uniteOrganisationRepository.findById(uoId).orElse(null);
        ArrayList<UniteOrganisation> listFils=new ArrayList<>();
        ArrayList<UniteOrganisation> listAll=new ArrayList<>();
        return uniteOrganisationMapper.toDto(uniteOrganisationService.findAllUoFils(uo,listAll,listFils));
    }


    @GetMapping("/unite-organisations/uo_fils_by_ville")
    @Timed
    public List<UniteOrganisationDTO> getAllUniteOrganisationsFilsAndVille(@RequestParam Long uoId,@RequestParam(required = false) Long villeId){
        log.debug("REST request to get a page of UniteOrganisations");
        UniteOrganisation uo=uniteOrganisationRepository.findById(uoId).orElse(null);
        ArrayList<UniteOrganisation> listFils=new ArrayList<>();
        ArrayList<UniteOrganisation> listAll=new ArrayList<>();
        return uniteOrganisationMapper.toDto(uniteOrganisationService.findAllUoFilsMultiCriteria(uo,listAll,listFils,villeId));
    }


    @GetMapping("/unite-organisationsByType/{typeId}")
    @Timed
    public List<UniteOrganisationDTO> getAllUniteOrganisationsByType(@PathVariable Long typeId) {
        log.debug("REST request to get a page of UniteOrganisations");
        return uniteOrganisationService.findByTypeUo(typeId);
    }

    /**
     * GET  /unite-organisations/:id : get the "id" uniteOrganisation.
     *
     * @param id the id of the uniteOrganisationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the uniteOrganisationDTO, or with status 404 (Not Found)
     */
    @GetMapping("/unite-organisations/{id}/enfants-optimise")
    @Timed
    public ResponseEntity<List<UniteOrganisationDTO>> getChildren(@PathVariable Long id, Pageable pageable) {
        Page<UniteOrganisationDTO> page = uniteOrganisationService.findChildren(id, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/unite-organisations/" + id + "/enfants-optimise");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/unite-organisations/{id}")
    @Timed
    public ResponseEntity<UniteOrganisationDTO> getUniteOrganisation(@PathVariable Long id) {
        log.debug("REST request to get UniteOrganisation : {}", id);
        UniteOrganisationDTO uniteOrganisationDTO = uniteOrganisationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(uniteOrganisationDTO));
    }
    @GetMapping("/unite-organisations/codeFSE/{codeFSE}")
    @Timed
    public UniteOrganisationDTO getUniteOrganisationByCodeFSE(@PathVariable String codeFSE) {
        UniteOrganisationDTO uniteOrganisationDTO = uniteOrganisationMapper.toDto(uniteOrganisationService.findByCodeFSE(codeFSE));
        return uniteOrganisationDTO;
    }

    @GetMapping("/unite-organisations/user-organisation")
    @Timed
    public ResponseEntity<UniteOrganisationDTO> getUniteOrganisationOfUserConnecte() {
        UniteOrganisationDTO uniteOrganisationDTO = uniteOrganisationService.getUoDTOOfUserConnecte();
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(uniteOrganisationDTO));
    }

    @GetMapping("/unite-organisations/uosFils-by-uo")
    @Timed
    public List<UniteOrganisationDTO> getUoAllFilsByType(@RequestParam(required = false) Long uoId,@RequestParam TypeUO type) {
        log.debug("REST request to get a page of UniteOrganisations Uo fils by uo");
        return uniteOrganisationService.findUoAllFilsByType(uoId,type);


    }

    @GetMapping("/unite-organisations/pere-by-type")
    @Timed
    public UniteOrganisationDTO getPereByType(@RequestParam(required = false) Long uoId,@RequestParam(required = false) TypeUO libelleTypeUO) {
        log.debug("REST request to get a page of UniteOrganisations");
        return uniteOrganisationService.findPereByType(uoId,libelleTypeUO);
    }
    /**
     * DELETE  /unite-organisations/:id : delete the "id" uniteOrganisation.
     *
     * @param id the id of the uniteOrganisationDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/unite-organisations/{id}")
    @Timed
    public ResponseEntity<Void> deleteUniteOrganisation(@PathVariable Long id) {
        log.debug("REST request to delete UniteOrganisation : {}", id);
        uniteOrganisationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    @GetMapping("/solde-distribution")
    @Timed
    public SommeSolde getSommeSoldeDistribution(@RequestParam(required = false) Long pereUoId, @RequestParam(required = false) String typeUO) {
        return uniteOrganisationService.getSoldeDistribution(pereUoId,typeUO,null);
    }
    @GetMapping("/solde-marchands")
    @Timed
    public SommeSolde getSommeSoldeMarchand(@RequestParam(required = false) Long pereUoId, @RequestParam(required = false) String typeUO) {
        return uniteOrganisationService.getSommeSoldeMarchands( pereUoId,typeUO,null);
    }
    @GetMapping("/allSolde")
    @Timed
    public HashMap getAllSolde(@RequestParam(required =true) Long pereUoId) {
        return uniteOrganisationService.getAllSolde(pereUoId);
    }

    @GetMapping("/findByProfilService")
    @Timed
    public List<UniteOrganisationDTO> findByProfilService(@RequestParam Long profilServiceId,@RequestParam Long pereUoId) {
        return uniteOrganisationService.findByProfilService(profilServiceId,pereUoId);
    }

    @GetMapping("/unite-organisations/findByTypeUo-optimiser")
    @Timed
    public List<UniteOrganisationDTO> findOrganisationByTypeUoOptimise(@RequestParam String typeUo){
        log.debug("Request to get organisation by type uo****");
        return uniteOrganisationService.findAllOptimiseByTypeUo(typeUo);
    }

    // TODO: 29/06/2022 URI native de récupération des organisation avec filtre 
    
    @GetMapping("/unite-organisations-paginer-optimise")
    @Timed
    public ResponseEntity<List<UniteOrganisationDTO>> getAllUniteOrganisationsByPageOptimise(@RequestParam(required = false)String libelleUniteOrganisation,@RequestParam(required = false)String codeUO,@RequestParam(required = false)Integer numeroOrdre,@RequestParam(required = false)String typeUO,@RequestParam(required = false) Long pereUoId,@RequestParam(required = false) Long groupeId,@RequestParam(required = false) Long villeId,@RequestParam(required = false) String adresse, Pageable pageable) {
        log.debug("request to get UO with pereuo {}",pereUoId);
        if(pereUoId!=null){
            Page<UniteOrganisationDTO> page=uniteOrganisationService.findAllByPereUoIdOptimise(libelleUniteOrganisation,codeUO,numeroOrdre,typeUO,pereUoId,groupeId,villeId,adresse,pageable);
            log.debug("page-------"+page);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/unite-organisations-paginer");
            return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        }
        Page<UniteOrganisationDTO> page=uniteOrganisationService.findAllOptimise(libelleUniteOrganisation,codeUO,numeroOrdre,typeUO,groupeId,villeId,adresse,pageable);
        log.debug("page--------"+page);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/unite-organisations-paginer");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    @GetMapping("/unite-organisations-optimise/{id}")
    @Timed
    public ResponseEntity<UniteOrganisationDTO> getUniteOrganisationOptimise(@PathVariable Long id) {
        log.debug("REST request to get UniteOrganisation : {}", id);
        UniteOrganisationDTO uniteOrganisationDTO = uniteOrganisationService.findOneOptimse(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(uniteOrganisationDTO));
    }
    @GetMapping("/unite-organisations/pere-by-type-optimise")
    @Timed
    public UniteOrganisationDTO getPereByTypeOptimise(@RequestParam(required = false) Long uoId,@RequestParam(required = false) TypeUO libelleTypeUO) {
        log.debug("REST request to get a page of UniteOrganisations");
        return uniteOrganisationService.findPereByTypeOptimise(uoId,libelleTypeUO);
    }

    @GetMapping("/unite-organisationsByType-optimise/{typeId}")
    @Timed
    public List<UniteOrganisationDTO> getAllUniteOrganisationsByTypeOptimise(@PathVariable Long typeId) {
        log.debug("REST request to get a page of UniteOrganisations");
        return uniteOrganisationService.findByTypeUoOptimise(typeId);
    }







}
