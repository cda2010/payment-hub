package com.m2i.pti.jobScheduler;

import com.m2i.pti.service.HistoriqueService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;



public class HistoriqueSoldeClient implements Serializable, Job {
    @Autowired
    HistoriqueService historiqueService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        this.historiqueService.historiqueSoldeClient();
        System.out.println("historique Solde Client saved !!!!!");
    }
}
