package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.OauthClientDetails;
import com.m2i.pti.domain.OauthClientSecret;
import com.m2i.pti.repository.OauthClientDetailsRepository;
import com.m2i.pti.repository.ProfilRepository;
import com.m2i.pti.service.CredentialApiService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.swagger.annotations.ApiParam;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * REST controller for managing CredentialApi.
 */
@RestController
@RequestMapping("/api")
public class CredentialApiResource {

    private final Logger log = LoggerFactory.getLogger(CredentialApiResource.class);

    private static final String ENTITY_NAME = "credentialApi";

    private final CredentialApiService credentialApiService;

    private final OauthClientDetailsRepository oauthClientDetailsRepository;

    private final PasswordEncoder passwordEncoder;

    private final ProfilRepository profilRepository;

    public CredentialApiResource(CredentialApiService credentialApiService, OauthClientDetailsRepository oauthClientDetailsRepository, PasswordEncoder passwordEncoder, ProfilRepository profilRepository) {
        this.credentialApiService = credentialApiService;
        this.oauthClientDetailsRepository = oauthClientDetailsRepository;
        this.passwordEncoder = passwordEncoder;
        this.profilRepository = profilRepository;
    }

    /**
     * POST  /credential-apis : Create a new credentialApi.
     *
     * @param credentialApi the credentialApi to create
     * @return the ResponseEntity with status 201 (Created) and with body the new credentialApi, or with status 400 (Bad Request) if the credentialApi has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/credential-apis")
    @Timed
    public ResponseEntity<OauthClientDetails> createCredentialApi(@RequestBody OauthClientDetails credentialApi) throws URISyntaxException {
        log.debug("REST request to save CredentialApi : {}", credentialApi);
        OauthClientDetails oauthClientDetails= new OauthClientDetails();
        oauthClientDetails=oauthClientDetailsRepository.findClient_id(credentialApi.getClient_id());
        if (oauthClientDetails != null) {
            throw new BadRequestAlertException("A new credentialApi cannot already have an ID", ENTITY_NAME, "idexists");
        }
        OauthClientDetails result = credentialApiService.save(credentialApi);
        return ResponseEntity.created(new URI("/api/credential-apis/" + result.getClient_id()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getClient_id()))
            .body(result);
    }

    /**
     * PUT  /credential-apis : Updates an existing credentialApi.
     *
     * @param oauthClientDetails the credentialApi to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated credentialApi,
     * or with status 400 (Bad Request) if the credentialApi is not valid,
     * or with status 500 (Internal Server Error) if the credentialApi couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/credential-apis")
    @Timed
    public ResponseEntity<OauthClientDetails> updateCredentialApi(@RequestBody OauthClientDetails oauthClientDetails) throws URISyntaxException {
        log.debug("REST request to update CredentialApi : {}", oauthClientDetails);
        OauthClientDetails oauthClientDet= new OauthClientDetails();
        oauthClientDet=oauthClientDetailsRepository.findClient_id(oauthClientDetails.getClient_id());
        if (oauthClientDet == null) {
            return createCredentialApi(oauthClientDetails);
        }
        OauthClientDetails result = credentialApiService.save(oauthClientDetails);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, oauthClientDetails.getClient_id()))
            .body(result);
    }

    /**
     * GET  /credential-apis : get all the credentialApis.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of credentialApis in body
     */
    @GetMapping("/credential-apis")
    @Timed
    public ResponseEntity<List<OauthClientDetails>> getAllCredentialApis(@ApiParam Pageable pageable) {
        Page<OauthClientDetails> page = oauthClientDetailsRepository.findAllOauth(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/credential-apis");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /credential-apis/:id : get the "id" credentialApi.
     *
     * @param id the id of the credentialApi to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the credentialApi, or with status 404 (Not Found)
     */
    @GetMapping("/credential-apis/{id}")
    @Timed
    public ResponseEntity<OauthClientDetails> getCredentialApi(@PathVariable String id) {
        log.debug("-----1------REST request to get CredentialApi : {}", id);
        OauthClientDetails oauthClientDetails = oauthClientDetailsRepository.findClient_id(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(oauthClientDetails));
    }
    @GetMapping("/oauth-client")
    @Timed
    public ResponseEntity<OauthClientDetails> getOauthclient(@RequestParam(required = true) String client_id) {
        log.debug("--------2-----REST request to get CredentialApi : {}", client_id);
        OauthClientDetails oauthClientDetails = oauthClientDetailsRepository.findClient_id(client_id);
        log.debug("------result-"+oauthClientDetails);
        log.debug("------pomm-"+oauthClientDetailsRepository.findClient_id(client_id));
        if (oauthClientDetails!=null) {
            return ResponseUtil.wrapOrNotFound(Optional.of(oauthClientDetails));
        }
        return null;
             //   ResponseUtil.wrapOrNotFound(Optional.ofNullable(oauthClientDetails));
    }

    /**
     * DELETE  /credential-apis/:id : delete the "id" credentialApi.
     *
     * @param id the id of the credentialApi to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/credential-apis/{id}")
    @Timed
    public ResponseEntity<Void> deleteCredentialApi(@PathVariable String id) {
        log.debug("REST request to delete CredentialApi : {}", id);
        oauthClientDetailsRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }
    // liste des oauh_client_details
    @GetMapping("/oauth-client-details")
    @Timed
    public ResponseEntity<List<OauthClientDetails>> getAllOauthClientDetails(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of CredentialApis");
        Page<OauthClientDetails> page = oauthClientDetailsRepository.findAll(pageable);
        log.debug("----------"+page);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/credential-apis");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/oauth-client/secret")
    @Timed
    public OauthClientSecret getClientSecret() {
        OauthClientSecret oauthClientSecret=new OauthClientSecret();
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRST01234567890".toCharArray();

        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String hashClientSecret = passwordEncoder.encode(sb.toString());
        oauthClientSecret.setClientSecret(hashClientSecret);
        return oauthClientSecret;
    }
}
