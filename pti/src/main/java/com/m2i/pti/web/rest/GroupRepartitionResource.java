package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.GroupRepartition;
import com.m2i.pti.service.GroupRepartitionService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.GroupRepartitionDTO;
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

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing GroupRepartition.
 */
@RestController
@RequestMapping("/api")
public class GroupRepartitionResource {

    private final Logger log = LoggerFactory.getLogger(GroupRepartitionResource.class);

    private static final String ENTITY_NAME = "groupRepartition";

    private final GroupRepartitionService groupRepartitionService;

    public GroupRepartitionResource(GroupRepartitionService groupRepartitionService) {
        this.groupRepartitionService = groupRepartitionService;
    }

    /**
     * POST  /group-repartitions : Create a new groupRepartition.
     *
     * @param groupRepartitionDTO the groupRepartitionDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new groupRepartitionDTO, or with status 400 (Bad Request) if the groupRepartition has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/group-repartitions")
    @Timed
    public ResponseEntity<GroupRepartitionDTO> createGroupRepartition(@RequestBody GroupRepartitionDTO groupRepartitionDTO) throws URISyntaxException {
        log.debug("REST request to save GroupRepartition : {}", groupRepartitionDTO);
        if (groupRepartitionDTO.getId() != null) {
            throw new BadRequestAlertException("A new groupRepartition cannot already have an ID", ENTITY_NAME, "idexists");
        }
        GroupRepartitionDTO result = groupRepartitionService.save(groupRepartitionDTO);
        return ResponseEntity.created(new URI("/api/group-repartitions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /group-repartitions : Updates an existing groupRepartition.
     *
     * @param groupRepartitionDTO the groupRepartitionDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated groupRepartitionDTO,
     * or with status 400 (Bad Request) if the groupRepartitionDTO is not valid,
     * or with status 500 (Internal Server Error) if the groupRepartitionDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/group-repartitions")
    @Timed
    public ResponseEntity<GroupRepartitionDTO> updateGroupRepartition(@RequestBody GroupRepartitionDTO groupRepartitionDTO) throws URISyntaxException {
        log.debug("REST request to update GroupRepartition : {}", groupRepartitionDTO);
        if (groupRepartitionDTO.getId() == null) {
            return createGroupRepartition(groupRepartitionDTO);
        }
        GroupRepartitionDTO result = groupRepartitionService.save(groupRepartitionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, groupRepartitionDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /group-repartitions : get all the groupRepartitions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of groupRepartitions in body
     */
    @GetMapping("/group-repartitions")
    @Timed
    public ResponseEntity<List<GroupRepartitionDTO>> getAllGroupRepartitions(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of GroupRepartitions");
        Page<GroupRepartitionDTO> page = groupRepartitionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/group-repartitions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    @GetMapping("/group-repartitions/all")
    @Timed
    public List<GroupRepartition> getAll() {
        return groupRepartitionService.findAll();
    }

    /**
     * GET  /group-repartitions/:id : get the "id" groupRepartition.
     *
     * @param id the id of the groupRepartitionDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the groupRepartitionDTO, or with status 404 (Not Found)
     */
    @GetMapping("/group-repartitions/{id}")
    @Timed
    public ResponseEntity<GroupRepartitionDTO> getGroupRepartition(@PathVariable Long id) {
        log.debug("REST request to get GroupRepartition : {}", id);
        GroupRepartitionDTO groupRepartitionDTO = groupRepartitionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(groupRepartitionDTO));
    }

    /**
     * DELETE  /group-repartitions/:id : delete the "id" groupRepartition.
     *
     * @param id the id of the groupRepartitionDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/group-repartitions/{id}")
    @Timed
    public ResponseEntity<Void> deleteGroupRepartition(@PathVariable Long id) {
        log.debug("REST request to delete GroupRepartition : {}", id);
        groupRepartitionService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
