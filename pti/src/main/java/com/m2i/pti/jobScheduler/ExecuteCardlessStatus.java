/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.jobScheduler;

import com.m2i.pti.domain.EvenementOperation;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.domain.enumeration.TypeServiceLibelle;
import com.m2i.pti.mobile.dto.OperationCardlessDTO;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.EvenementOperationRepository;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.HistoriqueService;
import java.io.IOException;
import com.m2i.pti.mobile.dto.ListOperationCardlessDTO;
import com.m2i.pti.service.TransactionComptePtiService;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.xml.bind.JAXBException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author cda
 */
public class ExecuteCardlessStatus implements Serializable, Job {

    @Autowired
    MobileService mobileService;
    @Autowired
    ApiService apiService;
    @Autowired
    EvenementOperationRepository evenementOperationRepository;
    @Autowired
    TransactionComptePtiService transactionComptePtiService;

    public void paiementExecuteCardlestatus(String entyCode) throws IOException, NoSuchAlgorithmException, JAXBException {
        EvenementOperation evenement = null;
        List<EvenementOperation> listeEvt = evenementOperationRepository.findOperationNonLeveeEtNonEntraitementCardLess(String.valueOf(TypeServiceLibelle.RETRAIT_CARDLESS));
        List<OperationCardlessDTO> listeOpCat = new ArrayList();
        ListOperationCardlessDTO listeOpCardless = new ListOperationCardlessDTO();
        for (EvenementOperation evt : listeEvt) {
            evt.setEnTraitement(Boolean.TRUE);

            Operation oper = evt.getOperation();
            if (oper.getCompteSrc().getCompteAutre() != null) {
                Long ifExiste = transactionComptePtiService.findExisteOperation(oper.getCodeOperation(), oper.getCompteSrc().getCompteAutre().getId());
                if (ifExiste <= 0) {
                    evt.setEstLevee(1);
                    evt.setEnTraitement(Boolean.FALSE);
                    evenementOperationRepository.save(evt);
                }
            }

        }

    }

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        try {
            JobDataMap data = jec.getJobDetail().getJobDataMap();
            String entityCode = data.getString("PARAMETRE1");
            paiementExecuteCardlestatus(entityCode);
        } catch (IOException ex) {
            Logger.getLogger(ExecuteCardlessStatus.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ExecuteCardlessStatus.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(ExecuteCardlessStatus.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
