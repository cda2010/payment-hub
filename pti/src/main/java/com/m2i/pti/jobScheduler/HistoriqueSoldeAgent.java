package com.m2i.pti.jobScheduler;

import com.m2i.pti.service.HistoriqueService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

public class HistoriqueSoldeAgent implements Serializable, Job {
    @Autowired
    HistoriqueService historiqueService;
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.historiqueService.historiqueSoldeAgent();
        System.out.println("historique Solde Agent saved !!!!!");
    }
}
