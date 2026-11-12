package com.m2i.pti.jobScheduler;

import com.m2i.pti.service.HistoriqueService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import org.quartz.JobDataMap;

public class HistoriqueTableauSoldeJob implements Serializable, Job {

    @Autowired
    HistoriqueService historiqueService;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        String codeUo = data.getString("PARAMETRE1");
        this.historiqueService.historiqueTableauSolde(codeUo);
        System.out.println("historique Tableau Solde saved !!!!!");
    }
}
