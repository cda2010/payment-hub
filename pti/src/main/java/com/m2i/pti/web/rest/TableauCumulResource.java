package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.enumeration.TypeRegle;
import com.m2i.pti.service.TableauCumulService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.TableauCumulDTO;
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
 * REST controller for managing TableauCumul.
 */
@RestController
@RequestMapping("/api")
public class TableauCumulResource {

    private final Logger log = LoggerFactory.getLogger(TableauCumulResource.class);

    private static final String ENTITY_NAME = "tableauCumul";

    private final TableauCumulService tableauCumulService;

    public TableauCumulResource(TableauCumulService tableauCumulService) {
        this.tableauCumulService = tableauCumulService;
    }

    /**
     * POST  /tableau-cumuls : Create a new tableauCumul.
     *
     * @param tableauCumulDTO the tableauCumulDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new tableauCumulDTO, or with status 400 (Bad Request) if the tableauCumul has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/tableau-cumuls")
    @Timed
    public ResponseEntity<TableauCumulDTO> createTableauCumul(@RequestBody TableauCumulDTO tableauCumulDTO) throws URISyntaxException {
        log.debug("REST request to save TableauCumul : {}", tableauCumulDTO);
        if (tableauCumulDTO.getId() != null) {
            throw new BadRequestAlertException("A new tableauCumul cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TableauCumulDTO result = tableauCumulService.save(tableauCumulDTO);
        return ResponseEntity.created(new URI("/api/tableau-cumuls/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /tableau-cumuls : Updates an existing tableauCumul.
     *
     * @param tableauCumulDTO the tableauCumulDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated tableauCumulDTO,
     * or with status 400 (Bad Request) if the tableauCumulDTO is not valid,
     * or with status 500 (Internal Server Error) if the tableauCumulDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/tableau-cumuls")
    @Timed
    public ResponseEntity<TableauCumulDTO> updateTableauCumul(@RequestBody TableauCumulDTO tableauCumulDTO) throws URISyntaxException {
        log.debug("REST request to update TableauCumul : {}", tableauCumulDTO);
        if (tableauCumulDTO.getId() == null) {
            return createTableauCumul(tableauCumulDTO);
        }
        TableauCumulDTO result = tableauCumulService.save(tableauCumulDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, tableauCumulDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /tableau-cumuls : get all the tableauCumuls.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of tableauCumuls in body
     */
    @GetMapping("/tableau-cumuls")
    @Timed
    public ResponseEntity<List<TableauCumulDTO>> getAllTableauCumuls(Pageable pageable, @RequestParam(required = false) Long typeServiceId, @RequestParam(required = false) Long canalId, @RequestParam(required = false) Long clientBeneficiaireId, @RequestParam(required = false) Long clientEmetteurId,
                                                                     @RequestParam(required = false) Long uoEmId, @RequestParam(required = false) Long uoAcqId) {
        log.debug("REST request to get a page of TableauFrais");
        Page<TableauCumulDTO> page = tableauCumulService.findAllByCriteria(pageable, typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmId, uoAcqId);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/tableau-frais");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/tableau-cumuls/all")
    @Timed
    public List<TableauCumulDTO> findAll(@RequestParam(required = false) TypeRegle type) {
        if (type != null) return tableauCumulService.findByTypeRegle(type);
        return tableauCumulService.findAll();
    }

  /*  @GetMapping("/tableau-cumuls/search")
    @Timed
    public List<TableauCumulDTO> getAllTableauCumulsSearch(@RequestParam TypeRegle typeRegle, @RequestParam(required = false) Long typeServiceId, @RequestParam(required = false) Long canalId, @RequestParam(required = false) Long clientBeneficiaireId, @RequestParam(required = false) Long clientEmetteurId,
                                                           @RequestParam(required = false) Long uoEmId, @RequestParam(required = false) Long uoAcqId, @RequestParam(required = false) Temps temps) {
      return tableauCumulService.findByCriteria(typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmId, uoAcqId,typeRegle,temps);
    }*/

    /**
     * GET  /tableau-cumuls/:id : get the "id" tableauCumul.
     *
     * @param id the id of the tableauCumulDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the tableauCumulDTO, or with status 404 (Not Found)
     */
    @GetMapping("/tableau-cumuls/{id}")
    @Timed
    public ResponseEntity<TableauCumulDTO> getTableauCumul(@PathVariable Long id) {
        log.debug("REST request to get TableauCumul : {}", id);
        TableauCumulDTO tableauCumulDTO = tableauCumulService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(tableauCumulDTO));
    }

    /**
     * DELETE  /tableau-cumuls/:id : delete the "id" tableauCumul.
     *
     * @param id the id of the tableauCumulDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/tableau-cumuls/{id}")
    @Timed
    public ResponseEntity<Void> deleteTableauCumul(@PathVariable Long id) {
        log.debug("REST request to delete TableauCumul : {}", id);
        tableauCumulService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
