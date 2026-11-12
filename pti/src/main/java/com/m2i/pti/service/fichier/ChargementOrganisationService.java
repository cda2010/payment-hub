package com.m2i.pti.service.fichier;

import com.m2i.pti.domain.ChargementFichier;
import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.repository.ChargementFichierRepository;
import com.m2i.pti.service.easyBatch.Read;
import com.m2i.pti.service.easyBatch.ReaderOrganisationService;
import com.m2i.pti.web.rest.ChargementMessageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChargementOrganisationService {
    private final Logger log = LoggerFactory.getLogger(ChargementMessageResource.class);

    private static final String ENTITY_NAME = "chargementOrganisation";
    ReaderOrganisationService readerOrganisationService;
    private final ChargementFichierRepository chargementFichierRepository;
    private static final String CHARGEMENT_PATTERN = "^(?:[0-9]+;?[0-9]+;?(.+)@(.+)+;[a-zA-Z]+$)";

    private final Read readService;

    public ChargementOrganisationService(ChargementFichierRepository chargementFichierRepository, Read readService) {
        this.chargementFichierRepository = chargementFichierRepository;
        this.readService = readService;
    }
    public Integer findByHashFichier(String hash) {
        return chargementFichierRepository.findByHashFichier(hash);
    };

    public Integer findByNomFichier(String toUpperCase) {
        return chargementFichierRepository.findByNomFichier(toUpperCase);

    };

    @Transactional(readOnly = true)
    public Page<ChargementFichier> findChargementPage(Long id, Pageable pageable) {
        Page<ChargementFichier> ChargementFichier = null;
        System.out.println("--------OKKKK-------" + id);
        ChargementFichier = chargementFichierRepository.findAllByOrganisationIdPage(id, pageable);

        return ChargementFichier;
    }

    public ChargementFichier findOne(Long id){
        return chargementFichierRepository.findById(id).orElse(null);
    }

    public ChargementFichier save(ChargementFichier chargementFichier){
        return chargementFichierRepository.save(chargementFichier);
    }

}
