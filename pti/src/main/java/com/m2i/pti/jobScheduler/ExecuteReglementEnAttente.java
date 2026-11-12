package com.m2i.pti.jobScheduler;

import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.repository.TypeServiceRepository;
import com.m2i.pti.service.TypeServiceService;
import com.m2i.pti.service.dto.OperationOptimiseDTO;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

public class ExecuteReglementEnAttente implements Serializable, Job  {
    private final Logger log = LoggerFactory.getLogger(ExecuteReglementEnAttente.class);

    @Autowired
    OperationRepository operationRepository;

    @Autowired
    TypeServiceRepository typeServiceRepository;

    public void rejetReglementEnAttente(){
        TypeService typeService=typeServiceRepository.findTopByCode("REGLEMENT");
        if(typeService!=null && typeService.getId()!=null){
            List<OperationOptimiseDTO> listOp=operationRepository.findOperationReglementEnAttente("ENATTENTE_VALIDATION",typeService.getId());
            if(listOp.size()>0){
                listOp.forEach(op->{
                    Operation operation=operationRepository.findByOperationId(op.getId());
                    if(operation!=null){
                        operation.setStatut(StatutOperation.REJETE);
                        System.out.println("OPERA---"+op.getId());
                        System.out.println("OPERA---"+op.getStatut());
                        operationRepository.save(operation);
                    }
                });
            }
        }
    }
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.debug("Reversion des opérations expirées");
        rejetReglementEnAttente();
    }
}
