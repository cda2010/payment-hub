/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.scheduler.service;

import java.util.regex.Pattern;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.m2i.pti.domain.HistExecutionJob;
import com.m2i.pti.domain.PtiJobDetails;
import com.m2i.pti.repository.PtiJobDetailsRepository;
import com.m2i.pti.service.HistExecutionJobService;
import com.m2i.pti.service.util.Util;

/**
 *
 * @author cda
 */
@Service
public class MyJobListener implements JobListener{

   private  String name;
public static final String LISTENER_NAME = "dummyJobListenerName";
  HistExecutionJobService histExecutionJobService;
PtiJobDetailsRepository ptiJobDetailsRepository;
  private final Logger log = LoggerFactory.getLogger(MyJobListener.class);
    public MyJobListener(HistExecutionJobService histExecutionJobService,PtiJobDetailsRepository ptiJobDetailsRepository) {
        this.histExecutionJobService = histExecutionJobService;
        this.ptiJobDetailsRepository=ptiJobDetailsRepository;
    }
  

   
   @Override
    public String getName() {
        return LISTENER_NAME;
    }

   @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        // do something with the event
    }

   @Override
    public void jobWasExecuted(JobExecutionContext context,
            JobExecutionException jobException) {
		//String jobName = context.getJobDetail().getJobDataMap().getString("jobName");
		//System.out.println("Job : " + jobName + " is finished...");


	String jobName = context.getJobDetail().getKey().toString();
		System.out.println("Job : " + jobName + " is finished...");
 log.debug("Job : " + jobName + " is finished...");
String[] job = jobName.split(Pattern.quote(".")); // Split on period.




		HistExecutionJob histExecutionJob=new HistExecutionJob();
                histExecutionJob.setDebutExecution(Util.dateTozoneDateTime(context.getFireTime()));
                if ( context.getJobRunTime()>0){
                      histExecutionJob.setFinExecution(Util.dateTozoneDateTime(context.getFireTime()).plusSeconds(context.getJobRunTime()));
                }else{
                    histExecutionJob.setFinExecution(Util.dateTozoneDateTime(context.getFireTime()));
                }
              

  
    
          log.debug("Job "+jobName+" end: " + context.getJobRunTime());
        System.out.println("MyJob end: " + context.getJobRunTime() );
            PtiJobDetails       ptiJobDetails =  this.ptiJobDetailsRepository.findByJobName(job[1].toUpperCase());
            histExecutionJob.setPtiJobDetail(ptiJobDetails);
           
             histExecutionJobService.save(histExecutionJob);
            //   histExecutionJob.setPtiJobDetail(context.getTrigger().);
        // do something with the event
    }

   @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // do something with the event
    }
    
}
