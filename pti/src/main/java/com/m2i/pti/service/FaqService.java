package com.m2i.pti.service;

import com.m2i.pti.domain.Faq;
import com.m2i.pti.repository.FaqRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing Faq.
 */
@Service
@Transactional
public class FaqService {

    private final Logger log = LoggerFactory.getLogger(FaqService.class);

    private final FaqRepository faqRepository;

    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    /**
     * Save a faq.
     *
     * @param faq the entity to save
     * @return the persisted entity
     */
    public Faq save(Faq faq) {
        log.debug("Request to save Faq : {}", faq);
        return faqRepository.save(faq);
    }

    /**
     *  Get all the faqs.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Faq> findAll(Pageable pageable) {
        log.debug("Request to get all Faqs");
        return faqRepository.findAll(pageable);
    }

    /**
     *  Get one faq by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Faq findOne(Long id) {
        log.debug("Request to get Faq : {}", id);
        return faqRepository.findById(id).orElse(null);
    }

    /**
     *  Delete the  faq by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Faq : {}", id);
        faqRepository.deleteById(id);
    }
}
