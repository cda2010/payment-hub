package com.m2i.pti.jobScheduler;

import com.m2i.pti.service.ReleveCompteService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;

public class ReleveCompteJob implements Serializable, Job {

    @Autowired
    ReleveCompteService releveCompteService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        
        this.releveCompteService.generateReleveOfPastMonth();
      
    }
}
