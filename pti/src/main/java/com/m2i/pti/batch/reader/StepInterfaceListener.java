/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.m2i.pti.batch.reader;

import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.service.ChargementTransfertService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author USER_M2I
 */
@Component
public class StepInterfaceListener implements StepExecutionListener {



    @Autowired
    ChargementTransfertService chargementTransfertService;

    public StepInterfaceListener() {
    }

    @Override
    public void beforeStep(StepExecution se) {
   
    }

    @Override
    public ExitStatus afterStep(StepExecution se) {
          
        return ExitStatus.COMPLETED;
    }

  

}
