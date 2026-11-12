package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.service.TemplateMessageService;
import com.m2i.pti.service.CommissionService;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.TemplateMessageDTO;
import io.swagger.annotations.ApiParam;
import io.github.jhipster.web.util.ResponseUtil;
import java.io.IOException;
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
import java.util.ArrayList;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing TemplateMessage.
 */
@RestController
@RequestMapping("/api")
public class TemplateMessageResource {

    private final Logger log = LoggerFactory.getLogger(TemplateMessageResource.class);

    private static final String ENTITY_NAME = "templateMessage";

    private final TemplateMessageService templateMessageService;
 private final UniteOrganisationService uniteOrganisationService;
private CommissionService commissionService;
    private final UniteOrganisationRepository uniteOrganisationRepository;
    public TemplateMessageResource( CommissionService commissionService,TemplateMessageService templateMessageService,UniteOrganisationRepository uniteOrganisationRepository,UniteOrganisationService uniteOrganisationService) {
        this.templateMessageService = templateMessageService;
        this.uniteOrganisationRepository=uniteOrganisationRepository;
        this.uniteOrganisationService=uniteOrganisationService;
        this.commissionService=commissionService;
        
        
    }

    /**
     * POST  /template-messages : Create a new templateMessage.
     *
     * @param templateMessageDTO the templateMessageDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new templateMessageDTO, or with status 400 (Bad Request) if the templateMessage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/template-messages")
    @Timed
    public ResponseEntity<TemplateMessageDTO> createTemplateMessage(@RequestBody TemplateMessageDTO templateMessageDTO) throws URISyntaxException {
        log.debug("REST request to save TemplateMessage : {}", templateMessageDTO);
        if (templateMessageDTO.getId() != null) {
            throw new BadRequestAlertException("A new templateMessage cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TemplateMessageDTO result = templateMessageService.save(templateMessageDTO);
        return ResponseEntity.created(new URI("/api/template-messages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /template-messages : Updates an existing templateMessage.
     *
     * @param templateMessageDTO the templateMessageDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated templateMessageDTO,
     * or with status 400 (Bad Request) if the templateMessageDTO is not valid,
     * or with status 500 (Internal Server Error) if the templateMessageDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/template-messages")
    @Timed
    public ResponseEntity<TemplateMessageDTO> updateTemplateMessage(@RequestBody TemplateMessageDTO templateMessageDTO) throws URISyntaxException {
        log.debug("REST request to update TemplateMessage : {}", templateMessageDTO);
        if (templateMessageDTO.getId() == null) {
            return createTemplateMessage(templateMessageDTO);
        }
        TemplateMessageDTO result = templateMessageService.save(templateMessageDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, templateMessageDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /template-messages : get all the templateMessages.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of templateMessages in body
     */
    @GetMapping("/template-messages")
    @Timed
    public ResponseEntity<List<TemplateMessageDTO>> getAllTemplateMessages(@ApiParam Pageable pageable,@RequestParam(required = false) Long typeServiceId) {
        log.debug("REST request to get a page of TemplateMessages");
        Page<TemplateMessageDTO> page = templateMessageService.findAll(pageable,typeServiceId);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/template-messages");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /template-messages/:id : get the "id" templateMessage.
     *
     * @param id the id of the templateMessageDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the templateMessageDTO, or with status 404 (Not Found)
     */
    @GetMapping("/template-messages/{id}")
    @Timed
    public ResponseEntity<TemplateMessageDTO> getTemplateMessage(@PathVariable Long id) {
        log.debug("REST request to get TemplateMessage : {}", id);
        TemplateMessageDTO templateMessageDTO = templateMessageService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(templateMessageDTO));
    }

    /**
     * DELETE  /template-messages/:id : delete the "id" templateMessage.
     *
     * @param id the id of the templateMessageDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/template-messages/{id}")
    @Timed
    public ResponseEntity<Void> deleteTemplateMessage(@PathVariable Long id) {
        log.debug("REST request to delete TemplateMessage : {}", id);
        templateMessageService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
     @GetMapping("/unite-organisations/pere-by-typex")
    @Timed
    public List<Long>  getPereByTypeX(@RequestParam Long uoId,@RequestParam Long operationId) throws IOException {
         UniteOrganisation uo= uniteOrganisationRepository.findById(uoId).orElse(null);
       ArrayList<Long> list= new ArrayList();
      commissionService.repartitionCommissionWithId(uoId, operationId);
         // log.debug("REST request to get a page of UniteOrganisations{}",u.getLibelleUniteOrganisation());
        //return uniteOrganisationService.findAllUoParentsIds(uo,list);
        return null;
    }
}
