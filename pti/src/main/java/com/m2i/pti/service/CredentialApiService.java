package com.m2i.pti.service;

import com.m2i.pti.domain.CredentialApi;
import com.m2i.pti.domain.OauthClientDetails;
import com.m2i.pti.repository.CredentialApiRepository;
import com.m2i.pti.repository.OauthClientDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing CredentialApi.
 */
@Service
@Transactional
public class CredentialApiService {

    private final Logger log = LoggerFactory.getLogger(CredentialApiService.class);

    private final CredentialApiRepository credentialApiRepository;

    private final OauthClientDetailsRepository oauthClientDetailsRepository;

    private final ApiService apiService;


    public CredentialApiService(CredentialApiRepository credentialApiRepository, OauthClientDetailsRepository oauthClientDetailsRepository, ApiService apiService) {
        this.credentialApiRepository = credentialApiRepository;
        this.oauthClientDetailsRepository = oauthClientDetailsRepository;
        this.apiService = apiService;
    }

    /**
     * Save a credentialApi.
     *
     * @param oauthClientDetails the entity to save
     * @return the persisted entity
     */
    public OauthClientDetails save(OauthClientDetails oauthClientDetails) {
        log.debug("Request to save CredentialApi : {}", oauthClientDetails);
        oauthClientDetails=oauthClientDetailsRepository.save(oauthClientDetails);
        apiService.allowMachine(oauthClientDetails.getAdresse_authorise());
        return oauthClientDetailsRepository.save(oauthClientDetails);

    }

    /**
     *  Get all the credentialApis.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<CredentialApi> findAll(Pageable pageable) {
        log.debug("Request to get all CredentialApis");
        return credentialApiRepository.findAll(pageable);
    }

    /**
     *  Get one credentialApi by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public CredentialApi findOne(Long id) {
        log.debug("Request to get CredentialApi : {}", id);
        return credentialApiRepository.findById(id).orElse(null);
    }

    /**
     *  Delete the  credentialApi by id.
     *
     *  @param id the id of the entity
     */
    public void delete(String id) {
        log.debug("Request to delete CredentialApi : {}", id);
        //credentialApiRepository.deleteById(id);
        oauthClientDetailsRepository.deleteById(id);
    }
}
