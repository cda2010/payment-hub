package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Lot;
import com.m2i.pti.service.LotService;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.service.dto.LotDTO;
import com.m2i.pti.service.mapper.LotMapper;
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
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LotResource {
    
    private static final String ENTITY_NAME = "Lot";
    private final Logger log = LoggerFactory.getLogger(LotResource.class);

    private final LotService lotService;

    private final LotMapper lotMapper;
    private final UniteOrganisationService uniteOrganisationService;

    public LotResource(LotService lotService, LotMapper lotMapper, UniteOrganisationService uniteOrganisationService) {
        this.lotService = lotService;
        this.lotMapper = lotMapper;
        this.uniteOrganisationService = uniteOrganisationService;
    }


    @PostMapping("/lots")
    @Timed
    public ResponseEntity<LotDTO> create(@RequestBody LotDTO lotDTO) throws URISyntaxException {
        lotDTO.setEtat(true);
        lotDTO.setDateOuverture(Instant.now());
        lotDTO.setEstImprime(false);
        lotDTO.setOrganisation(uniteOrganisationService.findById(lotDTO.getOrganisation().getId()));
        lotDTO.setNumero(lotService.getAlphaNumericString(8)+lotDTO.getDateOuverture().atZone(ZoneOffset.UTC).getSecond());
        LotDTO result = lotService.save(lotDTO);
        return ResponseEntity.created(new URI("/api/lots/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping("/lots")
    @Timed
    public ResponseEntity<LotDTO> update(@RequestBody LotDTO lotDTO) throws URISyntaxException {
        log.debug("REST request to update Lot : {}", lotDTO);
        if (lotDTO.getId() == null) {
            return create(lotDTO);
        }
        LotDTO result = lotService.save(lotDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, lotDTO.getId().toString()))
            .body(result);
    }

   
    // @GetMapping("/lots")
    // @Timed
    // public Page<LotDTO> getAll(Pageable pageable) {
    //     log.debug("REST request to get a page of Lots");
    //     return lotService.findAll(pageable);
    // }

    @GetMapping("/lots")
    @Timed
    public ResponseEntity<List<LotDTO>> getAllLotsPage(@ApiParam Pageable pageable,@RequestParam(required = false) String typeActeur) {
        log.debug("REST request to get a page of Lots");
        Page<Lot> page = lotService.findAll(pageable,typeActeur);

        List<LotDTO> listDtos = lotMapper.toDto(page.getContent());

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/lot");
        return new ResponseEntity<>(listDtos, headers, HttpStatus.OK);
    }

    
    @GetMapping("/lots/{id}")
    @Timed
    public ResponseEntity<LotDTO> getOne(@PathVariable Long id) {
        log.debug("REST request to get Lot : {}", id);
        LotDTO lotDTO = lotService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(lotDTO));
    }
    

    @DeleteMapping("/lots/{id}")
    @Timed
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("REST request to delete lot : {}", id);
        lotService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    
    @PutMapping("/lots/set-etat")
    @Timed
    public ResponseEntity<LotDTO> setEtat(@RequestBody LotDTO lotDTO) throws URISyntaxException {
        log.debug("REST request to update Lot {}", lotDTO);
        // LotDTO lotDTO = lotService.findOne(id);
        
        LotDTO result = lotService.setEtat(lotDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, lotDTO.getId().toString()))
            .body(result);
    }
    
}
