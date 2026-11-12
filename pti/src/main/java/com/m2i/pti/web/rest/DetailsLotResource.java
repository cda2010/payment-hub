package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.DetailsLot;
import com.m2i.pti.repository.DetailsLotRepository;
import com.m2i.pti.service.DetailsLotService;
import com.m2i.pti.service.dto.DetailsLotDTO;
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
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class DetailsLotResource {
    
    private static final String ENTITY_NAME = "DetailsLot";
    private final Logger log = LoggerFactory.getLogger(DetailsLotResource.class);

    private final DetailsLotService detailsLotService;
    private final DetailsLotRepository detailsLotRepository;

    public DetailsLotResource(DetailsLotService detailsLotService, DetailsLotRepository detailsLotRepository) {
        this.detailsLotService = detailsLotService;
        this.detailsLotRepository = detailsLotRepository;
    }


    @PostMapping("/details-lots")
    @Timed
    // public ResponseEntity<DetailsLotDTO> create(@RequestBody DetailsLotDTO detailsLotDTO) throws URISyntaxException {
    public ResponseEntity<DetailsLotDTO> create(@RequestBody DetailsLotDTO detailsLotDTO ) throws URISyntaxException {

        DetailsLotDTO result = detailsLotService.save(detailsLotDTO);
        return ResponseEntity.created(new URI("/api/details-lots/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping("/details-lots")
    @Timed
    public ResponseEntity<DetailsLotDTO> update(@RequestBody DetailsLotDTO detailsLotDTO) throws URISyntaxException {
        log.debug("REST request to update Lot : {}", detailsLotDTO);
        if (detailsLotDTO.getId() == null) {
            return create(detailsLotDTO);
        }
        DetailsLotDTO result = detailsLotService.save(detailsLotDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, detailsLotDTO.getId().toString()))
            .body(result);
    }

   
    // @GetMapping("/details-lots")
    // @Timed
    // public Page<DetailsLotDTO> getAll(Pageable pageable) {
    //     log.debug("REST request to get a page of DetailsLots");
    //     return detailsLotService.findAll(pageable);
    // }

    @GetMapping("/details-lots")
    @Timed
    public ResponseEntity<List<DetailsLot>> getAllDetailsLotPage(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of DetailsLot");
        Page<DetailsLot> page = detailsLotService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/details-lots");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/details-lots/of-lot/{id}")
    @Timed
    public List<DetailsLotDTO> getAllDetailsLotsOfLot(@PathVariable Long id) {
        log.debug("REST request to get all DetailsLots of lot");
        return detailsLotService.findDetailsLotByLotId(id);
    }

    
    @GetMapping("/details-lots/{id}")
    @Timed
    public ResponseEntity<DetailsLotDTO> getOne(@PathVariable Long id) {
        log.debug("REST request to get DetailsLots : {}", id);
        DetailsLotDTO detailsLot = detailsLotService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(detailsLot));
    }
    

    @DeleteMapping("/details-lots/{id}")
    @Timed
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("REST request to delete DetailsLot : {}", id);
        detailsLotService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    
}

