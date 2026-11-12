package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.Etiquette;
import com.m2i.omnicanal.application.domain.HistAppel;

import com.m2i.omnicanal.application.repository.EtiquetteRepository;
import com.m2i.omnicanal.application.repository.HistoriqueAppelRepository;
import com.m2i.omnicanal.application.service.dto.DataChartResponse;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;
import com.m2i.omnicanal.application.web.rest.util.PaginationUtil;
import com.m2i.omnicanal.model.Api;
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
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * REST controller for managing Etiquette.
 */
@RestController
@RequestMapping("/api")
public class HistoriqueAppelResource {

    private final Logger log = LoggerFactory.getLogger(HistoriqueAppelResource.class);

    private static final String ENTITY_NAME = "HistoriqueAppelRepository";

    private final HistoriqueAppelRepository historiqueAppelRepository;

    public HistoriqueAppelResource(HistoriqueAppelRepository historiqueAppelRepository) {
        this.historiqueAppelRepository = historiqueAppelRepository;
    }

    /**
     * GET /etiquettes : get all the historiqueAppelTop10.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of
     * etiquettes in body
     */
    @GetMapping("/historiqueAppelTop10")
    @Timed
    public List<HistAppel> getTop10HistoriqueAppel() {
        log.debug("REST request to get all historiqueAppelTop10");
        List<HistAppel> page = historiqueAppelRepository.findTop10();

        return page;

    }
 @GetMapping("/apiTotalRequest")
    @Timed
    public ResponseEntity<List<DataChartResponse>> getNombreTotalCall() {
      
        List<DataChartResponse> listOp = historiqueAppelRepository.findNombrePerFonction();
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(listOp));

    }
    @GetMapping("/apiTotalPerCodeErr")
    @Timed
    public ResponseEntity<List<DataChartResponse>> getnombrePerCodeErrorCall() {
      
        List<DataChartResponse> listOp = historiqueAppelRepository.findNombrePerCodeError();
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(listOp));

    }

    @GetMapping("/historiqueAppelAll")
    @Timed
    public List<HistAppel> getAllHistoriqueAppel() {
        log.debug("REST request to get all historiqueAppelTop10");
        List<HistAppel> page = historiqueAppelRepository.getAllHistoriqueAppel();

        return page;
    }

    @GetMapping("/historiqueAppelForDay")
    @Timed
    public List<HistAppel> getTodayHistoriqueAppel() {
        log.debug("REST request to get all historiqueAppelForDay");
        List<HistAppel> page = historiqueAppelRepository.getTodayHistoriqueAppel();
        return page;
    }
    
}
