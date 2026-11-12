package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.config.Constants;
import com.m2i.pti.mobile.dto.MDataReturn;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.service.MachineService;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.service.dto.MachineDTO;
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
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing Machine.
 */
@RestController
@RequestMapping("/api")
public class MachineResource {

    private static final String ENTITY_NAME = "machine";
    private final Logger log = LoggerFactory.getLogger(MachineResource.class);
    private final MachineService machineService;

    public MachineResource(MachineService machineService) {
        this.machineService = machineService;
    }

    /**
     * POST  /machine : Create a new machine.
     *
     * @param machineDTO the machineDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new machineDTO, or with status 400 (Bad Request) if the machine has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/machines")
    @Timed
    public ResponseEntity<MachineDTO> createMachine(@Valid @RequestBody MachineDTO machineDTO) throws URISyntaxException, IOException {
        log.debug("REST request to save Machine : {}", machineDTO);
        if (machineDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new machine cannot already have an ID")).body(null);
        }
        MachineDTO result = machineService.save(machineDTO);
        return ResponseEntity.created(new URI("/api/machines/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /machine : Updates an existing machine.
     *
     * @param machineDTO the machineDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated machineDTO,
     * or with status 400 (Bad Request) if the machineDTO is not valid,
     * or with status 500 (Internal Server Error) if the machineDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/machines")
    @Timed
    public ResponseEntity<MachineDTO> updateMachine(@Valid @RequestBody MachineDTO machineDTO) throws URISyntaxException, IOException {
        log.debug("REST request to update Machine : {}", machineDTO);
        if (machineDTO.getHostId() == null) {
            throw new CustomParameterizedException("L'identifiant de la machine ne peut pas être vide");
        }
        machineService.activate(machineDTO);
        return ResponseEntity.ok(machineDTO);
    }

    @PutMapping("/machine-updated")
    @Timed
    public ResponseEntity<MachineDTO> updateMachineNew(@Valid @RequestBody MachineDTO machineDTO){
        log.debug("REST request to update Machine : {}", machineDTO);
        if (machineDTO.getHostId() == null) {
            throw new CustomParameterizedException("L'identifiant de la machine ne peut pas être vide");
        }
        machineDTO=machineService.updateMachineNew(machineDTO);
        return ResponseEntity.ok(machineDTO);
    }

    /**
     * Renvoie le certificat d'une machine pour téléchargement
     *
     * @param uuid : L'identifiant de la machine
     * @return Le certificat sous forme d'un tableau de bytes
     * @throws URISyntaxException
     * @throws IOException
     */
    @GetMapping("/machines/{uuid:" + Constants.UUID_REGEX + "}")
    @Timed
    public MachineDTO getCertificat(@PathVariable(name = "uuid") String uuid) throws URISyntaxException, IOException {
        log.debug("REST request to update Machine : {}", uuid);
        return machineService.retrieveCertificat(uuid);
    }
  @GetMapping("/machines/verif_tokenpti")
    @Timed
    public MDataReturn verifTokenPti(@RequestParam String token, @RequestParam String loginData) throws URISyntaxException, IOException {
         log.debug("REST verif token : {}", token);
          log.debug("REST verif logindata : {}", loginData);
       return machineService.verifTokenPti(loginData, token);
       
    }

    @GetMapping("/machines/verif_token")
    @Timed
    public Boolean verifToken(@RequestParam String token, @RequestParam String loginData) throws URISyntaxException, IOException {
       return machineService.verifToken(loginData, token);
    }

    /**
     * GET  /machine : get all the machine.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of machine in body
     */
    @GetMapping("/machines")
    @Timed
    public ResponseEntity<List<MachineDTO>> getAllMachine(@ApiParam Pageable pageable,@RequestParam(required = false)  String adresse) {
        log.debug("REST request to get a page of Machine");
        Page<MachineDTO> page = machineService.findAll(pageable,adresse);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/machines");
        return new ResponseEntity<>(page.getContent().stream().map(m -> {
            try {
                m.setCurrentUser(new String(Base64.getDecoder().decode(m.getCurrentUser())));
                m.setHostName(new String(Base64.getDecoder().decode(m.getHostName())));
            } catch (Exception e) {

            }
            return m;
        }).collect(Collectors.toList()), headers, HttpStatus.OK);
    }


    @GetMapping("/machines-optimise")
    @Timed
    public ResponseEntity<List<MachineDTO>> getAllMachineOptimise(@ApiParam Pageable pageable,@RequestParam(required = false)  String adresse) {
        log.debug("REST request to get a page of Machine");
        Page<MachineDTO> page = machineService.findAllOptimise(pageable,adresse);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/machines-optimise");
        return new ResponseEntity<>(page.getContent().stream().map(m -> {
            try {
                m.setCurrentUser(new String(Base64.getDecoder().decode(m.getCurrentUser())));
                m.setHostName(new String(Base64.getDecoder().decode(m.getHostName())));
            } catch (Exception e) {
            }
            return m;
        }).collect(Collectors.toList()), headers, HttpStatus.OK);
    }


    /**
     * GET  /machine/:id : get the "id" machine.
     *
     * @param id the id of the machineDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the machineDTO, or with status 404 (Not Found)
     */
    @GetMapping("/machines/{id:" + Constants.ID_REGEX + "}")
    @Timed
    public ResponseEntity<MachineDTO> getMachine(@PathVariable Long id) {
        log.debug("REST request to get Machine : {}", id);
        MachineDTO machineDTO = machineService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(machineDTO));
    }

    /**
     * DELETE  /machine/:id : disable the "id" machine.
     *
     * @param id the id of the machineDTO to disable
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/machines/{id:" + Constants.ID_REGEX + "}")
    @Timed
    public ResponseEntity<Void> disableMachine(@PathVariable Long id) throws IOException {
        log.debug("REST request to delete Machine : {}", id);
        machineService.disable(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @DeleteMapping("/machines/delete")
    @Timed
    @Secured({AuthoritiesConstants.DELETE_MACHINE})
    public ResponseEntity<Void> deleteMachine(@RequestParam Long id){
        log.debug("REST request to delete Machine : {}", id);
        machineService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
