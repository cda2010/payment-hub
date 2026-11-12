package com.m2i.pti.service;

import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.EvenementOperation;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.enumeration.TypeEvenementOperation;
import com.m2i.pti.repository.CompteRepository;
import com.m2i.pti.repository.EvenementOperationRepository;

import java.math.BigDecimal;

import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.service.dto.EvenementOperationDTO;
import com.m2i.pti.service.mapper.EvenementOperationMapper;

import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Service Implementation for managing EvenementOperation.
 */
@Service
@Transactional
public class EvenementOperationService {

    private final Logger log = LoggerFactory.getLogger(EvenementOperationService.class);

    private final EvenementOperationRepository evenementOperationRepository;

    private final CompteRepository compteRepository;

    private final EvenementOperationMapper evenementOperationMapper;

    private final OperationRepository operationRepository;

    public EvenementOperationService(EvenementOperationRepository evenementOperationRepository, CompteRepository compteRepository, EvenementOperationMapper evenementOperationMapper, OperationRepository operationRepository) {
        this.evenementOperationRepository = evenementOperationRepository;
        this.compteRepository = compteRepository;
        this.evenementOperationMapper = evenementOperationMapper;
        this.operationRepository = operationRepository;
    }

    /**
     * Save a evenementOperation.
     *
     * @param evenementOperation the entity to save
     * @return the persisted entity
     */
    public EvenementOperation save(EvenementOperation evenementOperation) {
        log.debug("Request to save EvenementOperation : {}", evenementOperation);
        return evenementOperationRepository.save(evenementOperation);
    }

    /**
     * Get all the evenementOperations.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<EvenementOperation> findAll() {
        log.debug("Request to get all EvenementOperations");
        return evenementOperationRepository.findAll();
    }

    /**
     * Get one evenementOperation by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public EvenementOperation findOne(Long id) {
        log.debug("Request to get EvenementOperation : {}", id);
        return evenementOperationRepository.findById(id).orElse(null);
    }

    /**
     * Delete the evenementOperation by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete EvenementOperation : {}", id);
        evenementOperationRepository.deleteById(id);
    }

    public BigDecimal getEve(Compte cpte) {
        BigDecimal bgDec = BigDecimal.ZERO;
        bgDec = evenementOperationRepository.findSommeEvenementOperation(cpte.getId(), TypeEvenementOperation.DOIT.name());
        return bgDec;

    }

    @Transactional(readOnly = true)
    public Page<EvenementOperationDTO> findEvenementByCompteAutre(Long compteAutreId, ZonedDateTime dateDebut, ZonedDateTime dateFin, String telephone, Integer esLevee, Pageable pageable) {
        log.debug("Request to get some Evenements");
        Compte compte = new Compte();
        if (compteAutreId != null) {
            compte = compteRepository.findByCompteAutreId(compteAutreId);
        }
        if (dateDebut != null && dateFin != null) {
            return evenementOperationRepository.fndByCompteAndCriteria(compte.getId(), dateDebut, dateFin, telephone, esLevee, pageable)
                    .map(evenementOperationMapper::toDto);
        } else if (dateDebut != null) {
            return evenementOperationRepository.fndByCompteAndCriteria(compte.getId(), dateDebut, dateDebut.plusHours(23), telephone, esLevee, pageable)
                    .map(evenementOperationMapper::toDto);
        } else {
            return evenementOperationRepository.fndByCompteAndCriteria(compte.getId(), dateDebut, dateFin, telephone, esLevee, pageable)
                    .map(evenementOperationMapper::toDto);
        }

    }

    @Transactional
    public EvenementOperation evenementClient(Long operationId) {
        EvenementOperation evenementOperation = new EvenementOperation();
        if (operationId != null) {

            Operation operation = operationRepository.findByOperationId(operationId);

        } else {
            throw new CustomParameterizedException("Opération introuvable!");
        }

        return evenementOperation;
    }
    
    
      @Transactional
    public EvenementOperation saveEvt(Operation operation,Compte cpte,String typeEvenement, BigDecimal montant,String motif ,String login  ) {
        EvenementOperation evenementOperation = new EvenementOperation();
        if (operation != null) {
          evenementOperation.setOperation(operation);
          evenementOperation.setCompte(cpte);
          evenementOperation.setTypeEvenement(typeEvenement);
          evenementOperation.setMontant(montant);
          evenementOperation.setEstLevee(0);
          evenementOperation.setMotif(motif);
          evenementOperation.setLogin(login);
          evenementOperationRepository.save(evenementOperation);
        }

        return evenementOperation;
    }

}
