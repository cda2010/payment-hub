package com.m2i.pti.service;

import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Emetteur;
import com.m2i.pti.domain.Beneficiaire;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.OperationAudit;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.domain.enumeration.TypePersonne;
import com.m2i.pti.repository.OperationAuditRepository;
import com.m2i.pti.service.dto.ClientAllDTO;
import com.m2i.pti.service.dto.OperationAuditDTO;
import com.m2i.pti.service.mapper.OperationAuditMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.m2i.pti.domain.enumeration.ActionBlackList;
import com.m2i.pti.domain.BlackList;
import com.m2i.pti.domain.OperationSuivis;
import com.m2i.pti.mobile.dto.OperationSuiviClientDTO;
import java.time.ZonedDateTime;
import java.util.List;

import com.m2i.pti.service.kafka.MessageProducer;
import org.springframework.scheduling.annotation.Async;

/**
 * Service Implementation for managing OperationAudit.
 */
@Service
@Transactional
public class OperationAuditService {

    private final Logger log = LoggerFactory.getLogger(OperationAuditService.class);

    private final OperationAuditRepository operationAuditRepository;

    private final OperationAuditMapper operationAuditMapper;
    private final MessageProducer messageProducer;
    private final UserService userService;
    private final ClientService clientService;
    BlackListService blackListService;

    public OperationAuditService(MessageProducer messageProducer, BlackListService blackListService, ClientService clientService, OperationAuditRepository operationAuditRepository, OperationAuditMapper operationAuditMapper, UserService userService) {
        this.operationAuditRepository = operationAuditRepository;
        this.operationAuditMapper = operationAuditMapper;
        this.userService = userService;
        this.clientService = clientService;
        this.blackListService = blackListService;
        this.messageProducer = messageProducer;
    }

    /**
     * Save a operationAudit.
     *
     * @param operationAuditDTO the entity to save
     * @return the persisted entity
     */
    public OperationAuditDTO save(OperationAuditDTO operationAuditDTO) {
        log.info("Request to save OperationAudit : {}", operationAuditDTO);
        OperationAudit operationAudit = operationAuditMapper.toEntity(operationAuditDTO);
        operationAudit = operationAuditRepository.save(operationAudit);
        return operationAuditMapper.toDto(operationAudit);
    }

    /**
     * Get all the operationAudits.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<OperationAuditDTO> findAll(StatutOperation statutPrecedent,
            StatutOperation statutApres,
            Pageable pageable) {
        log.info("Request to get all OperationAudits");
        return operationAuditRepository.findByCriteria(statutPrecedent, statutApres, pageable)
                .map(operationAuditMapper::toDto);
    }

    public Page<OperationAuditDTO> findAllOptimiser(StatutOperation statutPrecedent,
                                           StatutOperation statutApres,
                                           Pageable pageable) {
        log.info("Request to get all OperationAudits");
        List<OperationAuditDTO> result = operationAuditRepository.findByCriteriaOptimiser(statutPrecedent, statutApres);
        int start= (int) pageable.getOffset();
        int end=(start+pageable.getPageSize())>result.size()?result.size():(start+pageable.getPageSize());
        Page<OperationAuditDTO> retour=new PageImpl<OperationAuditDTO>(result.subList(start,end),pageable,result.size());
        return retour;
    }

    /**
     * Get one operationAudit by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public OperationAuditDTO findOne(Long id) {
        log.info("Request to get OperationAudit : {}", id);
        OperationAudit operationAudit = operationAuditRepository.findById(id).orElse(null);
        return operationAuditMapper.toDto(operationAudit);
    }

    /**
     * Delete the operationAudit by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.info("Request to delete OperationAudit : {}", id);
        operationAuditRepository.deleteById(id);
    }
private OperationSuiviClientDTO createOpertionSuivi (Operation operation,Client cl){
     OperationSuiviClientDTO opsuivi=new OperationSuiviClientDTO();
      opsuivi.setOperationId(operation.getId());
      opsuivi.setClientId(cl.getId());
       return    opsuivi;            
}
  @Async("taskExecutor")
    public void rechercheSuivi(Operation operation) {
        Emetteur em;
        Beneficiaire ben;
        Client clem = null;
        Client cleben = null;
        Integer nombre = 0;

        if (operation.getEmetteur() != null) {
            em = operation.getEmetteur();
            if (em.getPersonne() != null) {
                if (em.getPersonne().getTypePersonne().equals(TypePersonne.PHYSIQUE)) { //PersonnePhysiqye
                    clem = clientService.findByPersonneId(em.getPersonne().getId());
                }
            }
        }
        if (operation.getBeneficaire() != null) {
            ben = operation.getBeneficaire();
            if (ben.getPersonne() != null) {
                if (ben.getPersonne().getTypePersonne().equals(TypePersonne.PHYSIQUE)) { //PersonnePhysiqye
                    cleben = clientService.findByPersonneId(ben.getPersonne().getId());
                }
            }
        }
        if (clem != cleben) {
            if (clem != null) {
                nombre = blackListService.countByClientAndActionBlackList(clem, ActionBlackList.FOLLOW);
                if (nombre > 0) {
                     
            
                    messageProducer.sendMessageOperation(createOpertionSuivi ( operation, clem));
                }
            }
            if (cleben != null) {
                nombre = blackListService.countByClientAndActionBlackList(cleben, ActionBlackList.FOLLOW);
                if (nombre > 0) {
                    messageProducer.sendMessageOperation(createOpertionSuivi ( operation, cleben));
                }
            }
        } else if (clem != null && cleben != null) {
            nombre = blackListService.countByClientAndActionBlackList(clem, ActionBlackList.FOLLOW);
            if (nombre > 0) {
                messageProducer.sendMessageOperation(createOpertionSuivi ( operation, clem));
            }

        }

    }

    
    
    
        @Async("taskExecutor")
    public void rechercheSuivi(Operation operation,Client cliemetteur, Client clibeneficiaire) {
        Emetteur em;
        Beneficiaire ben;
        Client clem = null;
        Client cleben = null;
        Integer nombre = 0;

        if (operation.getEmetteur() != null) {
            em = operation.getEmetteur();
            if (em.getPersonne() != null) {
                if (em.getPersonne().getTypePersonne().equals(TypePersonne.PHYSIQUE)) { //PersonnePhysiqye
                    clem = cliemetteur;
                }
            }
        }
        if (operation.getBeneficaire() != null) {
            ben = operation.getBeneficaire();
            if (ben.getPersonne() != null) {
                if (ben.getPersonne().getTypePersonne().equals(TypePersonne.PHYSIQUE)) { //PersonnePhysiqye
                    cleben = clibeneficiaire;
                }
            }
        }
        if (clem != cleben) {
            if (clem != null) {
                nombre = blackListService.countByClientAndActionBlackList(clem, ActionBlackList.FOLLOW);
                if (nombre > 0) {
                     
             
                    messageProducer.sendMessageOperation(createOpertionSuivi ( operation, clem));
                }
            }
            if (cleben != null) {
                nombre = blackListService.countByClientAndActionBlackList(cleben, ActionBlackList.FOLLOW);
                if (nombre > 0) {
                    messageProducer.sendMessageOperation(createOpertionSuivi ( operation, cleben));
                }
            }
        } else if (clem != null && cleben != null) {
            nombre = blackListService.countByClientAndActionBlackList(clem, ActionBlackList.FOLLOW);
            if (nombre > 0) {
                messageProducer.sendMessageOperation(createOpertionSuivi ( operation, clem));
            }

        }

    }
    
    
    
    public OperationAudit saveOpAudit(Operation operation, StatutOperation statutOperationPrecedent, StatutOperation statutOperation) {
        log.info("Sauvegarde l'audit de l'operation");
        OperationAudit operationAudit = new OperationAudit();
        operationAudit.operation(operation)
                .date(ZonedDateTime.now())
                .statutPrecedent(statutOperationPrecedent)
                .user(userService.getUserConnecte())
                .statutApres(statutOperation);
     
        return operationAuditRepository.save(operationAudit);
    }
}
