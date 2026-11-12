/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.m2i.pti.batch.reader.validation;

import com.m2i.pti.domain.BatchExecution;
import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.service.ChargementTransfertService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author USER_M2I
 */

public class StepInterfaceListener implements StepExecutionListener {



    @Autowired
    ChargementTransfertService chargementTransfertService;

    public StepInterfaceListener() {
    }

    @Override
    public void beforeStep(StepExecution se) {
          Long idChargement = se.getJobParameters().getLong("chargementId");
            ChargementTransfert ch = chargementTransfertService.findOne(idChargement);
        BatchExecution batchExecution=new BatchExecution();
        batchExecution.setId(se.getJobExecution().getJobId());
      //  ch.setBatchExecution(batchExecution);
        chargementTransfertService.save(ch);
    }

    @Override
    public ExitStatus afterStep(StepExecution se) {
          
        return ExitStatus.COMPLETED;
    }

  

}
