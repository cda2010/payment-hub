package com.m2i.pti.jobScheduler;

import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.OperationService;
import com.m2i.pti.service.ReleveCompteService;
import com.m2i.pti.service.easyBatch.Read;
import com.m2i.pti.web.rest.OperationResource;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.service.OperationAnnulationService;

public class ReversionOperationExipiree implements Serializable, Job {

    private final Logger log = LoggerFactory.getLogger(ReversionOperationExipiree.class);
    @Autowired
    OperationService operationService;

    @Autowired
    OperationRepository operationRepository;
    @Autowired
    Read readService;

    public void annulationOperationExpire() {
        List<String> typeService = new ArrayList();
        typeService.add("ACCOUNT_CASH");
        typeService.add("ENVOI_CASH");
        List<Operation> listeOperation = operationRepository.findOperationExpire(StatutOperation.ENATTENTE, LocalDate.now(), typeService);
        org.easybatch.core.job.Job job = (org.easybatch.core.job.Job) readService.getBatch(listeOperation, new OperationAnnulationService.OperationProcessor(operationService));
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.debug("Reversion des opérations expirées");
        annulationOperationExpire();
        //  this.operationService.annulationOperationExpire();

    }
}
