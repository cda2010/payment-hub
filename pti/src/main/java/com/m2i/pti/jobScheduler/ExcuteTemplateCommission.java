/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.jobScheduler;

import com.m2i.pti.domain.LigneReglement;
import com.m2i.pti.domain.Reglement;
import com.m2i.pti.domain.ReglementTemplate;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.User;
import com.m2i.pti.repository.CommissionRepository;
import com.m2i.pti.repository.FtpRepository;
import com.m2i.pti.repository.LigneReglementRepository;
import com.m2i.pti.service.CommissionService;
import com.m2i.pti.service.OperationService;
import com.m2i.pti.service.RecepteurAlarmeService;
import com.m2i.pti.service.ReglementService;
import com.m2i.pti.service.ReglementTemplateService;
import com.m2i.pti.service.TypeServiceService;
import com.m2i.pti.service.UserService;
import com.m2i.pti.service.dto.OperationDTO;
import com.m2i.pti.service.easyBatch.Read;
import com.m2i.pti.service.pentaho.ExecuteReplication;
import com.m2i.pti.service.pentaho.ReplicationDomaine;
import com.m2i.pti.web.rest.OperationResource;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author cda
 */
public class ExcuteTemplateCommission implements Serializable, Job {

    @Autowired
    ReglementTemplateService reglementTemplateService;
    @Autowired
    ReglementService reglementService;
    @Autowired
    TypeServiceService typeService;
    @Autowired
    UserService userService;
    @Autowired
    OperationService operationService;
    @Autowired
    Read readService;
    @Autowired
    LigneReglementRepository ligneReglementRepository;
    private final Logger log = LoggerFactory.getLogger(ExcuteTemplateCommission.class);
    @Autowired
    RecepteurAlarmeService recepteurAlarmeService;
  @Autowired
       CommissionService commissionService;
    @Autowired
        CommissionRepository commissionRepository;
    
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        log.debug("DEBUT DE L 'EXECUTION DE LA TACHE RECGLEMENT");
        List<ReglementTemplate> listeRegle = reglementTemplateService.findAll();
        log.debug("NOMBRE DE REGLE:" + listeRegle.size());
        for (ReglementTemplate regleTemplate : listeRegle) {
            log.debug("ENREGISTREMENT DU REGLEMENT");
            ZonedDateTime dateRegl = ZonedDateTime.now();
            Reglement reglement = new Reglement();
            reglement.setDateReglement(dateRegl);
            reglement.setStatut(false);
            reglement.setEstAnnuler(false);
            TypeService tpeService = typeService.findByCodeService(regleTemplate.getCodeService());
            User userConnecte = userService.getUserConnecteOptimise(regleTemplate.getLogin());
            operationService.userCanDoOperations(userConnecte);
            List<Long> listeOperation = new ArrayList();
            Reglement reg = reglementService.save(reglement);

            log.debug("RECUPERATION DES ORGANISATION CONCERNEE:");
            regleTemplate.getTextLigneReglementTemplate().getLigneReglementTemplate().getOrganisation().forEach(org -> {
                listeOperation.add(org.getId());
            });

            OperationDTO operationDTO = new OperationDTO();
            operationDTO.setCode(regleTemplate.getCodeService());
            operationDTO.setCanalLibelleCanal("MOBILE");
            readService.getBatch(listeOperation, new OperationResource.ReglementProcessor(commissionRepository, commissionService, tpeService, reglement, operationService, operationDTO, dateRegl, userConnecte));
            //   List<Long> listeOperation =   
            log.debug("FIN DE ENREGISTREMENT DU REGLEMENT");
            log.debug("DEBUT DE LA VALIDATION");
            List<LigneReglement> listeReglement = ligneReglementRepository.findByReglementIdAndValideIsFalse(reg.getId());
            try {
                if (listeReglement.size() > 0) {
                    reg.setEnTraitement(Boolean.TRUE);
                    reglementService.save(reg);
                    readService.getBatch(listeReglement, new OperationResource.ReglementValideProcessor(operationService, regleTemplate.getLogin(), userConnecte));
                    reg.setValiderPar(userConnecte.getLogin());
                    reg.setEstAnnuler(false);
                    reg.setStatut(true);
                    reg.setEnTraitement(Boolean.FALSE);
                    reglementService.save(reg);

                } else {
                    throw new CustomParameterizedException("Le règlement est annulé ");
                }

            } catch (Exception e) {
                //   reg.setEstAnnuler(reg);
                reg.setStatut(false);
                reglementService.save(reg);
            }
            log.debug("FIN DE LA VALIDATION");
        }
        String sms = "LE PAIEMENT DES COMMISSIONS EST TERMINE:";
        recepteurAlarmeService.sendAlarme(sms, "PAIEMENT_MASSE");
        log.debug("FIN DE LA TACHE");
    }

}
