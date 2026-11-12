package com.m2i.pti.service;

import com.m2i.pti.domain.ReglementTemplate;
import com.m2i.pti.repository.ReglementTemplateRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReglementTemplateService {
    private final Logger log = LoggerFactory.getLogger(ReglementTemplateService.class);
    private final ReglementTemplateRepository reglementTemplateRepository;
   
    public ReglementTemplateService(ReglementTemplateRepository reglementTemplateRepository) {
        this.reglementTemplateRepository = reglementTemplateRepository;
       
    }
    @Transactional
    public ReglementTemplate save(ReglementTemplate reglementTemplate) {
        log.debug("Request to save reglement : {}", reglementTemplate);
        reglementTemplate = reglementTemplateRepository.save(reglementTemplate);
        return reglementTemplate;
    }
    @Transactional(readOnly = true)
    public Page<ReglementTemplate> findAll(Pageable pageable) {
        log.debug("Request to get all ReglementTemplate");
        return reglementTemplateRepository.findAll(pageable);
    }
    @Transactional(readOnly = true)
    public ReglementTemplate findOne(Long id) {
        log.debug("Request to get ReglementTemplate : {}", id);
        return reglementTemplateRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        log.debug("Request to delete reglement : {}", id);
        reglementTemplateRepository.deleteById(id);
    }

    public List<ReglementTemplate> findAll() {
           return reglementTemplateRepository.findAll();
    }
   
}
