package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.TableauFraisService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.TableauFraisDTO;
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

/**
 * REST controller for managing TableauFrais.
 */
@RestController
@RequestMapping("/api")
public class TableauFraisResource {

    private final Logger log = LoggerFactory.getLogger(TableauFraisResource.class);

    private static final String ENTITY_NAME = "tableauFrais";

    private final TableauFraisService tableauFraisService;

    public TableauFraisResource(TableauFraisService tableauFraisService) {
        this.tableauFraisService = tableauFraisService;
    }

    /**
     * POST  /tableau-frais : Create a new tableauFrais.
     *
     * @param tableauFraisDTO the tableauFraisDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new tableauFraisDTO, or with status 400 (Bad Request) if the tableauFrais has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/tableau-frais")
    @Timed
    public ResponseEntity<TableauFraisDTO> createTableauFrais(@RequestBody TableauFraisDTO tableauFraisDTO) throws URISyntaxException {
        log.debug("REST request to save TableauFrais : {}", tableauFraisDTO);
        if (tableauFraisDTO.getId() != null) {
            throw new BadRequestAlertException("A new tableauFrais cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TableauFraisDTO result = tableauFraisService.save(tableauFraisDTO);
        return ResponseEntity.created(new URI("/api/tableau-frais/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /tableau-frais : Updates an existing tableauFrais.
     *
     * @param tableauFraisDTO the tableauFraisDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated tableauFraisDTO,
     * or with status 400 (Bad Request) if the tableauFraisDTO is not valid,
     * or with status 500 (Internal Server Error) if the tableauFraisDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/tableau-frais")
    @Timed
    public ResponseEntity<TableauFraisDTO> updateTableauFrais(@RequestBody TableauFraisDTO tableauFraisDTO) throws URISyntaxException {
        log.debug("REST request to update TableauFrais : {}", tableauFraisDTO);
        if (tableauFraisDTO.getId() == null) {
            return createTableauFrais(tableauFraisDTO);
        }
        TableauFraisDTO result = tableauFraisService.save(tableauFraisDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, tableauFraisDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /tableau-frais : get all the tableauFrais.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of tableauFrais in body
     */
    @GetMapping("/tableau-frais")
    @Timed
    public ResponseEntity<List<TableauFraisDTO>> getAllTableauFrais(Pageable pageable,@RequestParam(required = false) Long typeServiceId,@RequestParam(required = false) Long canalId,@RequestParam(required = false) Long clientBeneficiaireId,@RequestParam(required = false) Long clientEmetteurId,
                                                                    @RequestParam(required = false) Long uoEmId,@RequestParam(required = false) Long uoAcqId) {
        log.debug("REST request to get a page of TableauFrais");
        Page<TableauFraisDTO> page = tableauFraisService.findAllByCriteria(pageable,typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmId, uoAcqId);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/tableau-frais");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    @GetMapping("/tableau-frais/search")
    @Timed
    public List<TableauFraisDTO> getAllTableauFraisByMultiParm(@RequestParam(required = false) Long typeServiceId, @RequestParam(required = false) Long canalId
        , @RequestParam(required = false) Long clientBeneficiaire, @RequestParam(required = false) Long clientEmetteur, @RequestParam(required = false) List<Long> uOEmIds, @RequestParam(required = false) List<Long> uOAcqIds) {
        List<TableauFraisDTO> page = tableauFraisService.findAllByMultiC(typeServiceId, canalId, clientBeneficiaire, clientEmetteur, uOAcqIds, uOAcqIds);
        return page;
    }

    @GetMapping("/tableau-frais/all")
    @Timed
    public List<TableauFraisDTO> findAll(){
        return tableauFraisService.findAll();
    }

   /* @GetMapping("/tableau-frais/calcul-frais")
    @Timed
    public List<TableauFraisDTO> getAllTableauFraisOfOperation(@RequestParam(required = false) Long typeServiceId, @RequestParam(required = false) Long canalId
        , @RequestParam(required = false) Long clientBeneficiaire, @RequestParam(required = false) Long clientEmetteur, @RequestParam(required = false) Long uOEmId, @RequestParam(required = false) Long uOAcqId) {
        log.debug("REST request to get a page of TableauFrais "+clientBeneficiaire);
        List<TableauFraisDTO> page = tableauFraisService.deduireFrais(typeServiceId, canalId, clientBeneficiaire, clientEmetteur, uOEmId, uOAcqId);
        return page;
    }*/

    /**
     * GET  /tableau-frais/:id : get the "id" tableauFrais.
     *
     * @param id the id of the tableauFraisDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the tableauFraisDTO, or with status 404 (Not Found)
     */
    @GetMapping("/tableau-frais/{id}")
    @Timed
    public ResponseEntity<TableauFraisDTO> getTableauFrais(@PathVariable Long id) {
        log.debug("REST request to get TableauFrais : {}", id);
        TableauFraisDTO tableauFraisDTO = tableauFraisService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(tableauFraisDTO));
    }

    /**
     * DELETE  /tableau-frais/:id : delete the "id" tableauFrais.
     *
     * @param id the id of the tableauFraisDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/tableau-frais/{id}")
    @Timed
    public ResponseEntity<Void> deleteTableauFrais(@PathVariable Long id) {
        log.debug("REST request to delete TableauFrais : {}", id);
        tableauFraisService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
