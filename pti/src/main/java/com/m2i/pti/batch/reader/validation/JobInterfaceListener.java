/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.m2i.pti.batch.reader.validation;

import com.m2i.pti.domain.BatchExecution;
import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.LigneChargementCompte;
import com.m2i.pti.domain.enumeration.Etape;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import com.m2i.pti.service.ChargementTransfertService;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.LigneChargementTransfertService;
import com.m2i.pti.service.RecepteurAlarmeService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author USER_M2I
 */
public class JobInterfaceListener implements JobExecutionListener {

    @Autowired
    ChargementTransfertService chargementTransfertService;

    @Autowired
    LigneChargementTransfertService lignechargementTransfertService;
    @Autowired
    RecepteurAlarmeService recepteurAlarmeService;

    @Autowired
    CompteService compteService;

    @Override
    public void beforeJob(JobExecution je) {
        Long idChargement = je.getJobParameters().getLong("chargementId");
        ChargementTransfert ch = chargementTransfertService.findOne(idChargement);
        if (ch != null) {
            Compte cpt = compteService.findIfCompteExisteWithClient(ch.getCompteDebit(), ch.getClient());

            if (cpt.getCompteAutre() != null) {
                cpt = compteService.findCompteOne(cpt.getId());
                if (cpt.getCompteAutre() != null) {
                    ch.setSoldeCompteAvant(cpt.getCompteAutre().getSolde());
                }
                BatchExecution batchExecution = new BatchExecution();
                batchExecution.setId(je.getJobId());
                ch.setBatchExecution(batchExecution);
                chargementTransfertService.save(ch);
            }

        }

    }

    @Override
    public void afterJob(JobExecution je) {
        Long idChargement = je.getJobParameters().getLong("chargementId");
        if (je.getStatus().equals(BatchStatus.COMPLETED)) {
            ChargementTransfert ch = chargementTransfertService.findOne(idChargement);
            if (ch != null) {
                Compte cpt = compteService.findIfCompteExisteWithClient(ch.getCompteDebit(), ch.getClient());
                cpt = compteService.findCompteOne(cpt.getId());
                if (cpt.getCompteAutre() != null) {
                    ch.setSoldeCompteApres(cpt.getCompteAutre().getSolde());
                }
                Integer nbreOk = lignechargementTransfertService.findCompteByChargementTransfertIdAndStatutLigneChargement(idChargement, StatutLigneChargement.OC);
                Integer nbreNotOk = lignechargementTransfertService.findCompteByChargementTransfertIdAndStatutLigneChargement(idChargement, StatutLigneChargement.IC);
                Double montantCharge = lignechargementTransfertService.findSommeByChargementTransfertIdAndStatutLigneChargement(idChargement, StatutLigneChargement.OC);
                ch.setMontantCharger(montantCharge);
                ch.setNombreLigneErreur(nbreNotOk);
                ch.setNombreLigneCharge(nbreOk);
                ch.setStatut("V");
                ch.setEtape(Etape.PROCEED);
                String sms = "LE PAIEMENT EN MASSE REFERENCE:" + ch.getReference() + " est termine, "
                        + "======================================================"
                        + " Date debut: " + je.getCreateTime()
                        + " Solde du compte avant : " + ch.getSoldeCompteAvant()
                        + " Solde du compte après : " + ch.getSoldeCompteApres()
                        + " Date fin : " + je.getEndTime();
                recepteurAlarmeService.sendAlarme(sms, "PAIEMENT_MASSE");
                chargementTransfertService.save(ch);
            }

            /**
             * *Envoyer un message
             */
        }
    }

}
