package com.m2i.pti.service;

import com.m2i.pti.domain.OperationAudit;
import com.m2i.pti.domain.ParametreApp;
import com.m2i.pti.domain.User;
import com.m2i.pti.domain.enumeration.Acteur;
import com.m2i.pti.domain.enumeration.EtiquetteCompteCode;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.domain.enumeration.TypeParametre;
import com.m2i.pti.repository.OperationAuditRepository;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.repository.UserRepository;
import com.m2i.pti.service.util.RandomUtil;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JobSchedulerService {
    private final org.slf4j.Logger log = LoggerFactory.getLogger(JobSchedulerService.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final OperationRepository operationRepository;
    private final CommissionService commissionService;
    private final OperationService operationService;
    private final OperationAuditRepository operationAuditRepository;
    private final OperationAuditService operationAuditService;
    private final TauxChangeService tauxChangeService;
    private final UserRepository userRepository;
    private final ParametreAppService parametreAppService;

    public JobSchedulerService(OperationRepository operationRepository, CommissionService commissionService, OperationService operationService, OperationAuditRepository operationAuditRepository, OperationAuditService operationAuditService, TauxChangeService tauxChangeService, UserRepository userRepository, ParametreAppService parametreAppService) {
        this.operationRepository = operationRepository;
        this.commissionService = commissionService;
        this.operationService = operationService;
        this.operationAuditRepository = operationAuditRepository;
        this.operationAuditService = operationAuditService;
        this.tauxChangeService = tauxChangeService;
        this.userRepository = userRepository;
        this.parametreAppService = parametreAppService;
    }

    /**
     * Service de repartition des commissions ACQ et EM dans le compte principal des commissions des acteurs ayant participé à
     * Une Operation.
     */
  //  @Scheduled(cron = "* 2 * * * ?")
    public void scheduleTaskRepartitionCommissionsPTI() {
        commissionService.repartitionCommissionSurPti(Acteur.ACQ, EtiquetteCompteCode.CPTEATTENTECOMACQPTI);
        commissionService.repartitionCommissionSurPti(Acteur.EM, EtiquetteCompteCode.CPTEATTENTECOMEMTPTI);
    }

    /**
     * Service de mise à jour du taux de change
     * This is scheduled to get fired everyday, at 1:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void updateTauxChange() {
        tauxChangeService.updateAll();
    }

    /**
     * Mise à jour du statut des operaitons en attente
     */
    @Scheduled(cron = "* 5 * * * ?")
    public void miseAjourStatutOperation() {
        operationRepository.findByStatut(StatutOperation.ENCOURS).forEach(operation -> {
            List<OperationAudit> operationAudits = operationAuditRepository.findByOperationIdAndStatutPrecedentAndStatutApres(operation.getId(), StatutOperation.ENATTENTE, StatutOperation.ENCOURS);
            if (operationAudits.size() <= 2) {
                String otps[] = RandomUtil.generateOtp();
                operation.setStatut(StatutOperation.ENATTENTE);
                operation = operationRepository.save(operation);
                operationService.sendMessage(operation, Boolean.FALSE, otps[1]);
                operationAuditService.saveOpAudit(operation, StatutOperation.ENCOURS, StatutOperation.ENATTENTE);
                log.info("Mise en attente de l'operation " + operation.getCodeOperation());
            } else {
                log.info("Suspension de l'operation " + operation.getCodeOperation());
                operation.setStatut(StatutOperation.SUSPENDU);
                operation = operationRepository.save(operation);
                operationAuditService.saveOpAudit(operation, StatutOperation.ENCOURS, StatutOperation.SUSPENDU);
            }
        });
    }



    /**
     * Not activated users should be automatically disabled after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotConnectedUsers() {
        ParametreApp param = parametreAppService.findByTypeAndNom(TypeParametre.SYSTEM, "NBR_JOUR_USER_INACTIF");
        String nbr;
        if (param != null) {
            nbr = param.getValeur();
        } else nbr = "5";//5 jours par default
        List<User> users = userRepository.findAllByActivatedIsTrueAndDateDerniereConnexionBefore(ZonedDateTime.now().minus(Integer.parseInt(nbr), ChronoUnit.DAYS));

        for (User user : users) {
            user.setActivated(Boolean.TRUE);
            userRepository.save(user);
        }
    }

 //   @Scheduled(cron = "2 * * * * ?")
    public void scheduleTaskRepartitionCommissionsOminicanal() throws IOException {
        log.info("*****************Repartition de commsision OMNICANAL: ", dateTimeFormatter.format(LocalDateTime.now()) + "***************");
        commissionService.repartitionCommissionWithOmnicanal();
    }
}
