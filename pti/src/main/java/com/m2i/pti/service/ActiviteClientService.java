package com.m2i.pti.service;

import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.ActiviteClient;
import com.m2i.pti.domain.Personne;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.mobile.dto.MOperation;
import com.m2i.pti.repository.ActiviteClientRepository;
import com.m2i.pti.service.dto.ActiviteClientDTO;
import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing Canal.
 */
@Service
@Transactional
public class ActiviteClientService {

    private final Logger log = LoggerFactory.getLogger(ActiviteClientService.class);

    private final ActiviteClientRepository activiteClientRepository;
    TypeServiceService typeServiceService;

    public ActiviteClientService(ActiviteClientRepository activiteClientRepository, TypeServiceService typeServiceService) {
        this.activiteClientRepository = activiteClientRepository;
        this.typeServiceService = typeServiceService;

    }

    public ActiviteClient save(ActiviteClient activite) {
        log.debug("Request to save ActiviteClient : {}", activite);
        ActiviteClient activiteClient = this.activiteClientRepository.save(activite);
        return activiteClient;
    }

    /**
     * Save a canal.
     *
     * @param canalDTO the entity to save
     * @return the persisted entity
     */
    public ActiviteClient save(MOperation mOperation, Personne personne, StatutOperation statut, String codeFse) {
        ActiviteClient activiteClient;
        ZonedDateTime dateTime = ZonedDateTime.now();

        TypeService typeService = typeServiceService.findByService(mOperation.getTypeService());
        activiteClient = new ActiviteClient();
        activiteClient.setMontant(mOperation.getMontant());
        activiteClient.setDateOperation(dateTime);
        activiteClient.setTypeService(typeService);
        activiteClient.setPersonne(personne);
        activiteClient.setCodeFse(codeFse);

        activiteClient.setStatut(statut);
        activiteClient = this.activiteClientRepository.save(activiteClient);
        return activiteClient;
    }

    public List<ActiviteClientDTO> getActiviteClient(String codeTelephone, String telephone) {

        List<ActiviteClientDTO> listActiviteDto = activiteClientRepository.findActiviteClient(telephone, codeTelephone);

        return listActiviteDto;
    }
}
