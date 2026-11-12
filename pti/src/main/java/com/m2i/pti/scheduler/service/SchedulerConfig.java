/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.scheduler.service;

import java.io.IOException;
import java.util.Properties;
import javax.sql.DataSource;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;


/**
 *
 * @author cda
 */
@Configuration  
public class SchedulerConfig {
    @Autowired  
    private DataSource dataSource;  
  
    @Autowired  
    private ApplicationContext applicationContext;  
  
  
  
    /**
     * create scheduler factory 
     */  
    @Bean  
    public SchedulerFactoryBean schedulerFactoryBean() throws SchedulerException {  
  
        SchedulerJobFactory jobFactory = new SchedulerJobFactory();  
        jobFactory.setApplicationContext(applicationContext);  
  
        Properties properties = new Properties();  
      
        properties.setProperty("org.quartz.scheduler.instanceName", "MyInstanceName");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
           properties.setProperty("org.quartz.threadPool.makeThreadsDaemons", "true");

        SchedulerFactoryBean factory = new SchedulerFactoryBean();  
          factory.setAutoStartup(true);
        factory.setOverwriteExistingJobs(true);  
        factory.setQuartzProperties(properties);  
        factory.setJobFactory(jobFactory);  
       
        return factory;  
    }  
    
    
    
}
