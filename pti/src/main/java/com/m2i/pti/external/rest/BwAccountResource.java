

package com.m2i.pti.external.rest;
import com.m2i.pti.domain.BwAccount;
import com.m2i.pti.external.domain.*;
import com.m2i.pti.external.service.BwAccountService;

import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.m2i.partnerapp.domain.BwAccount}.
 */
@RestController
@RequestMapping("/api")
public class BwAccountResource {
    private final Logger log = LoggerFactory.getLogger(BwAccountResource.class);
    private static final String ENTITY_NAME = "bwAccount";
    private final BwAccountService bwAccountService;
    public BwAccountResource(BwAccountService bwAccountService) {
        this.bwAccountService = bwAccountService;
    }

    /**
     * {@code POST  /bw-accounts} : Create a new bwAccount.
     *
     * @param bwAccount the bwAccount to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new bwAccount, or with status {@code 400 (Bad Request)} if the bwAccount has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/bw-accounts")
    public ResponseEntity<BwAccount> createBwAccount(@Valid @RequestBody BwAccount bwAccount) throws URISyntaxException {
        log.debug("REST request to save BwAccount : {}", bwAccount);
        if (bwAccount.getId() != null) {
            throw new BadRequestAlertException("A new bwAccount cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BwAccount result = bwAccountService.save(bwAccount);
        return ResponseEntity.created(new URI("/api/bw-accounts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /bw-accounts} : Updates an existing bwAccount.
     *
     * @param bwAccount the bwAccount to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bwAccount,
     * or with status {@code 400 (Bad Request)} if the bwAccount is not valid,
     * or with status {@code 500 (Internal Server Error)} if the bwAccount couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/bw-accounts")
    public ResponseEntity<BwAccount> updateBwAccount(@Valid @RequestBody BwAccount bwAccount) throws URISyntaxException {
        log.debug("REST request to update BwAccount : {}", bwAccount);
        if (bwAccount.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        BwAccount result = bwAccountService.save(bwAccount);
       return ResponseEntity.created(new URI("/api/bw-accounts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
 @GetMapping("/bw-accounts")
    public ResponseEntity<List<BwAccount>> getAllBwAccounts(Pageable pageable, @RequestParam MultiValueMap<String, String> queryParams, UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get a page of BwAccounts");
        Page<BwAccount> page = bwAccountService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders( page,"/api/bw-accounts/");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
    /**
     * {@code GET  /bw-accounts} : get all the bwAccounts.
     *
     * @param cusId
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of bwAccounts in body.
     */
    @GetMapping("/bw-accounts/cus/{cusId}")
    public List<BwAccount> getAllBwAccountscusId(@PathVariable Long cusId) {
        log.debug("REST request to get a page of BwAccounts");
        List<BwAccount> listBwAccount = bwAccountService.findByBwCustomerId(cusId);
       
        return listBwAccount;
    }

    /**
     * {@code GET  /bw-accounts/:id} : get the "id" bwAccount.
     *
     * @param id the id of the bwAccount to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the bwAccount, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/bw-accounts/{id}")
    public ResponseEntity<BwAccount> getBwAccount(@PathVariable Long id) {
        log.debug("REST request to get BwAccount : {}", id);
        BwAccount bwAccount = bwAccountService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(bwAccount));
    }

    /**
     * {@code DELETE  /bw-accounts/:id} : delete the "id" bwAccount.
     *
     * @param id the id of the bwAccount to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/bw-accounts/{id}")
    public ResponseEntity<Void> deleteBwAccount(@PathVariable Long id) {
        log.debug("REST request to delete BwAccount : {}", id);
        bwAccountService.delete(id);
          return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
        
        //return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
