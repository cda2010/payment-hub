package com.m2i.pti.domain;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;




public class PtiJobDetails1 implements Job {

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        System.out.println("Hello Quartz! 123");
    }

  

    
  
}
