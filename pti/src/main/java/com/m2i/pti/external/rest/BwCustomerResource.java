package com.m2i.pti.external.rest;

import com.m2i.pti.external.service.*;
import com.m2i.pti.domain.BwCustomer;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;

/**
 * REST controller for managing {@link com.m2i.partnerapp.domain.BwCustomer}.
 */
@RestController
@RequestMapping("/api")
public class BwCustomerResource {

    private final Logger log = LoggerFactory.getLogger(BwCustomerResource.class);

    private static final String ENTITY_NAME = "bwCustomer";

    private final BwCustomerService bwCustomerService;
 private final UniteOrganisationService uniteOrganisationService;
    public BwCustomerResource(BwCustomerService bwCustomerService,UniteOrganisationService uniteOrganisationService) {
        this.bwCustomerService = bwCustomerService;
           this.uniteOrganisationService = uniteOrganisationService;
    }

    /**
     * {@code POST  /bw-customers} : Create a new bwCustomer.
     *
     * @param bwCustomer the bwCustomer to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and
     * with body the new bwCustomer, or with status {@code 400 (Bad Request)} if
     * the bwCustomer has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/bw-customers")
    public ResponseEntity<BwCustomer> createBwCustomer(@Valid @RequestBody BwCustomer bwCustomer) throws URISyntaxException {
        log.debug("REST request to save BwCustomer : {}", bwCustomer);
        if (bwCustomer.getId() != null) {
            throw new BadRequestAlertException("A new bwCustomer cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BwCustomer result = bwCustomerService.save(bwCustomer);
        return ResponseEntity.created(new URI("/api/bw-customers/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * {@code PUT  /bw-customers} : Updates an existing bwCustomer.
     *
     * @param bwCustomer the bwCustomer to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with
     * body the updated bwCustomer, or with status {@code 400 (Bad Request)} if
     * the bwCustomer is not valid, or with status
     * {@code 500 (Internal Server Error)} if the bwCustomer couldn't be
     * updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/bw-customers")
    public ResponseEntity<BwCustomer> updateBwCustomer(@Valid @RequestBody BwCustomer bwCustomer) throws URISyntaxException {
        log.debug("REST request to update BwCustomer : {}", bwCustomer);
        if (bwCustomer.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        BwCustomer result = bwCustomerService.save(bwCustomer);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * {@code GET  /bw-customers} : get all the bwCustomers.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the
     * list of bwCustomers in body.
     */
    /**
     * {@code GET  /bw-customers} : get all the bwCustomers.
     *
     * @param pageable
     * @param nom
     * @param prenom
     * @param telephone
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the
     * list of bwCustomers in body.
     */
    @GetMapping("/bw-customers/{nom}/{prenom}/{telephone}/{valide}")
    public ResponseEntity<List<BwCustomer>> getAllBwCustomers(Pageable pageable, @PathVariable String nom, @PathVariable String prenom, @PathVariable String telephone, @PathVariable Long valide) {
        log.debug("REST request to get a page of BwAccounts");
        UniteOrganisation uniteOrganisation = uniteOrganisationService.getUoOfUserConnecte();
        if ("0".equals(nom)) {
            nom = null;
        }
        if ("0".equals(prenom)) {
            prenom = null;
        }
        if ("0".equals(telephone)) {
            telephone = null;
        }
        boolean bool = (valide != 0);
       // UniteOrganisation fseDest = uniteOrganisationRepository.findById(uoId).orElse(null);//Fse Dest
     UniteOrganisation   fseUserConnect = uniteOrganisationService.findUoParentByType(uniteOrganisation, TypeUO.FSE);
        Page<BwCustomer> page = bwCustomerService.findAll(pageable, nom, prenom, telephone, bool, fseUserConnect.getId());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/bw-customers/");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /bw-customers/:id} : get the "id" bwCustomer.
     *
     * @param id the id of the bwCustomer to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with
     * body the bwCustomer, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/bw-customers/{id}")
    public ResponseEntity<BwCustomer> getBwCustomer(@PathVariable Long id) {
        log.debug("REST request to get BwCustomer : {}", id);
        BwCustomer bwCustomer = bwCustomerService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(bwCustomer));
    }

    /**
     * {@code DELETE  /bw-customers/:id} : delete the "id" bwCustomer.
     *
     * @param id the id of the bwCustomer to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/bw-customers/{id}")
    public ResponseEntity<Void> deleteBwCustomer(@PathVariable Long id) {
        log.debug("REST request to delete BwCustomer : {}", id);
        bwCustomerService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
