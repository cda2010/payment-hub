/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.jobScheduler;

import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.CommissionRepository;
import com.m2i.pti.service.CommissionService;
import com.m2i.pti.service.dto.CommissionDTO;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.Serializable;
import java.util.List;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author cda
 */
public class PaiementCommission implements Serializable, Job {

    private final Logger log = LoggerFactory.getLogger(PaiementCommission.class);

    @Autowired
    MobileService mobileService;

    @Autowired
    CommissionRepository commissionRepository;
    @Autowired
    CommissionService commissionService;

    @Transactional
    public void paiementCommissionResiduelle() throws IOException {
        List<CommissionDTO> listeCommission = commissionRepository.findByCommissionPasPayeAndEstComptabilise();
        commissionService.repartitionCommissionBatchWithId(listeCommission);

    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.debug("paiement des commissions residuelles");
        try {
            paiementCommissionResiduelle();
        } catch (IOException e) {

        }

        //  this.operationService.annulationOperationExpire();
    }
}
