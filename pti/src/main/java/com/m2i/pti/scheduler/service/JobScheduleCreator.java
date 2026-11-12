/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.scheduler.service;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Component;

import com.m2i.pti.domain.ParamExecutionJob;
/**
 *
 * @author cda
 */

@Component  
public class JobScheduleCreator {
    public JobDetail createJob(Class<? extends QuartzJobBean> jobClass, boolean isDurable,  
                               ApplicationContext context, String jobName, String jobGroup, List<ParamExecutionJob> listParam) {  
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();  
        factoryBean.setJobClass(jobClass);  
        factoryBean.setDurability(isDurable);  
        factoryBean.setApplicationContext(context);  
        factoryBean.setName(jobName);  
        factoryBean.setGroup(jobGroup);  
  
        // set job data map  
        JobDataMap jobDataMap = new JobDataMap();  
        jobDataMap.put(jobName + jobGroup, jobClass.getName());  
        
        if (listParam.size() > 0) {
                for (ParamExecutionJob lp : listParam) {
                    jobDataMap.put(lp.getCle(), lp.getValeur());
                }

            }
        
        factoryBean.setJobDataMap(jobDataMap);  
  
        factoryBean.afterPropertiesSet();  
  
        return factoryBean.getObject();  
    }  
  
    /** 
     * Create cron trigger. 
     * 
     * @param triggerName        Trigger name. 
     * @param startTime          Trigger start time. 
     * @param cronExpression     Cron expression. 
     * @param misFireInstruction Misfire instruction (what to do in case of misfire happens). 
     * @param priority 
     * @return {@link CronTrigger} 
     */  
    public CronTrigger createCronTrigger(String triggerName, Date startTime, String cronExpression, int misFireInstruction,int priority, List<ParamExecutionJob> listParam) {  
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();  
        factoryBean.setName(triggerName);  
        factoryBean.setStartTime(startTime);  
        factoryBean.setPriority(priority);
        factoryBean.setCronExpression(cronExpression);  
        factoryBean.setMisfireInstruction(misFireInstruction);  
        try {  
             JobDataMap jobDataMap = new JobDataMap();  
        
        
        if (listParam.size() > 0) {
                for (ParamExecutionJob lp : listParam) {
                    jobDataMap.put(lp.getCle(), lp.getValeur());
                }

            }
         factoryBean.setJobDataMap(jobDataMap);  
            factoryBean.afterPropertiesSet();  
        } catch (ParseException e) {  
            //log.error(e.getMessage(), e);  
        }  
        return factoryBean.getObject();  
    }  
  
    /** 
     * Create simple trigger. 
     * 
     * @param triggerName        Trigger name. 
     * @param startTime          Trigger start time. 
     * @param repeatTime         Job repeat period mills 
     * @param misFireInstruction Misfire instruction (what to do in case of misfire happens). 
     * @param priority 
     * @return {@link SimpleTrigger} 
     */  
    public SimpleTrigger createSimpleTrigger(String triggerName, Date startTime, Long repeatTime, int misFireInstruction,int priority,List<ParamExecutionJob> listParam) {  
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();  
        factoryBean.setName(triggerName);  
        factoryBean.setStartTime(startTime);  
        factoryBean.setRepeatInterval(repeatTime);  
        factoryBean.setPriority(priority);
        factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);  
        JobDataMap jobDataMap = new JobDataMap();  
        if (listParam.size() > 0) {
                for (ParamExecutionJob lp : listParam) {
                    jobDataMap.put(lp.getCle(), lp.getValeur());
                }
        }
        
        
        
        
        factoryBean.setJobDataMap(jobDataMap);  
        factoryBean.setMisfireInstruction(misFireInstruction);  
        factoryBean.afterPropertiesSet();  
        return factoryBean.getObject();  
    }  
}
