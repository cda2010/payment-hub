package com.m2i.pti.service.fichier;

import com.m2i.pti.domain.ChargementFichier;
import com.m2i.pti.domain.LigneMessage;
import com.m2i.pti.repository.ChargementFichierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChargementFichierService {
    private final Logger log = LoggerFactory.getLogger(ChargementFichierService.class);
    private final ChargementFichierRepository chargementFichierRepository;

    public ChargementFichierService(ChargementFichierRepository chargementFichierRepository) {
        this.chargementFichierRepository = chargementFichierRepository;
    }


    private ChargementFichier save(ChargementFichier chargementFichier){
        log.debug("Request to save ChargementFichier : {}",chargementFichier);
        return chargementFichierRepository.save(chargementFichier);
    }

    @Transactional(readOnly = true)
    public Page<ChargementFichier> findAll(Pageable pageable){
        log.debug("Request to get all LigneMessages");
        return chargementFichierRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public ChargementFichier findOne(Long id) {
        log.debug("Request to get ChargementFichier : {}", id);
        return chargementFichierRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        log.debug("Request to delete ChargementFichier : {}", id);
        chargementFichierRepository.deleteById(id);
    }


}
