package com.m2i.pti.service;

import com.m2i.pti.domain.LigneReglement;
import com.m2i.pti.domain.Reglement;
import com.m2i.pti.repository.ReglementRepository;
import com.m2i.pti.service.dto.ReglementDTO;
import com.m2i.pti.service.mapper.ReglementMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReglementService {
    private final Logger log = LoggerFactory.getLogger(ReglementService.class);
    private final ReglementRepository reglementRepository;
    private final ReglementMapper reglementMapper;
    public ReglementService(ReglementRepository reglementRepository, ReglementMapper reglementMapper) {
        this.reglementRepository = reglementRepository;
        this.reglementMapper = reglementMapper;
    }
    @Transactional
    public Reglement save(Reglement reglement) {
        log.debug("Request to save reglement : {}", reglement);
        reglement = reglementRepository.save(reglement);
        return reglement;
    }
    @Transactional(readOnly = true)
    public Page<ReglementDTO> findAll(Pageable pageable) {
        log.debug("Request to get all reglement");
        return reglementRepository.getAllReglement(pageable).map(reglementMapper::toDto);
    }
    @Transactional(readOnly = true)
    public Reglement findOne(Long id) {
        log.debug("Request to get Rezglement : {}", id);
        return reglementRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        log.debug("Request to delete reglement : {}", id);
        reglementRepository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public Page<LigneReglement> findLigne(Long id, Pageable pageable) {
        Page<LigneReglement> ligneReglements = null;
        ligneReglements = reglementRepository.getligneReglement(id,pageable);
        return ligneReglements;
    }
}
