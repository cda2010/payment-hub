/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.scheduler.service;

import com.m2i.pti.domain.ParamExecutionJob;
import com.m2i.pti.domain.PtiJobDetails;
import com.m2i.pti.repository.FournisseurServiceRepository;
import com.m2i.pti.repository.PtiJobDetailsRepository;
import com.m2i.pti.service.ParamExecutionJobService;
import com.m2i.pti.service.PtiJobDetailsService;
import com.m2i.pti.service.mapper.PtiJobDetailsMapper;
import java.util.Date;
import java.util.List;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import static org.quartz.impl.matchers.EverythingMatcher.allJobs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 *
 * @author cda
 */

@Service
public class SchedulerService {

    private final Logger log = LoggerFactory.getLogger(SchedulerService.class);
    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    MyJobListener myJobListener;
    private final PtiJobDetailsRepository ptiJobDetailsRepository;
    ParamExecutionJobService paramExecutionJobService;
    private final PtiJobDetailsMapper ptiJobDetailsMapper;
    private final FournisseurServiceRepository fournisseurServiceRepository;
    @Autowired
    private ApplicationContext context;

    @Autowired
    private JobScheduleCreator scheduleCreator;

    public SchedulerService(ParamExecutionJobService paramExecutionJobService, MyJobListener myJobListener, PtiJobDetailsRepository ptiJobDetailsRepository, PtiJobDetailsMapper ptiJobDetailsMapper, FournisseurServiceRepository fournisseurServiceRepository) {
        this.ptiJobDetailsRepository = ptiJobDetailsRepository;
        this.ptiJobDetailsMapper = ptiJobDetailsMapper;
        this.fournisseurServiceRepository = fournisseurServiceRepository;
        this.myJobListener = myJobListener;
        this.paramExecutionJobService = paramExecutionJobService;
    }

    public void scheduleNewJob(PtiJobDetails jobInfo) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            JobDetail jobDetail = JobBuilder.newJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()))
                    .usingJobData("jobName", jobInfo.getJobName())
                    .withIdentity(jobInfo.getJobName(), jobInfo.getNomGroupe()).build();

            List<ParamExecutionJob> listParam = paramExecutionJobService.findByPtiJobDetails(jobInfo);
            if (listParam.size() > 0) {
                for (ParamExecutionJob lp : listParam) {
                    jobDetail.getJobDataMap().put(lp.getCle(), lp.getValeur());
                }

            }
            if (!scheduler.checkExists(jobDetail.getKey())) {

                jobDetail = scheduleCreator.createJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()),
                        false, context, jobInfo.getJobName(), jobInfo.getNomGroupe(),listParam);

                Trigger trigger;
                if (jobInfo.getCronJob()) {
                    trigger = scheduleCreator.createCronTrigger(jobInfo.getJobName(), new Date(), jobInfo.getExpressionCron(),
                            CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING, jobInfo.getPriorite(),listParam);
                } else {
                    trigger = scheduleCreator.createSimpleTrigger(jobInfo.getJobName(), new Date(), jobInfo.getRepeatTime(),
                            SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT, jobInfo.getPriorite(),listParam);
                }

                scheduler.scheduleJob(jobDetail, trigger);
                jobInfo.setDisplayName(schedulerFactoryBean.getScheduler().getSchedulerName());
                //ptiJobDetailsRepository.save(jobInfo);  
            } else {
                log.error("scheduleNewJobRequest.jobAlreadyExist");
            }
        } catch (ClassNotFoundException e) {
            log.error("Class Not Found - {}", jobInfo.getJobClass(), e);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void updateScheduleJob(PtiJobDetails jobInfo) {
        Trigger newTrigger;

        try {
            List<ParamExecutionJob> listParam = paramExecutionJobService.findByPtiJobDetails(jobInfo);
            
            if (jobInfo.getCronJob()) {
                newTrigger = scheduleCreator.createCronTrigger(jobInfo.getJobName(), new Date(), jobInfo.getExpressionCron(),
                        CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING, jobInfo.getPriorite(),listParam);
                
            } else {
                newTrigger = scheduleCreator.createSimpleTrigger(jobInfo.getJobName(), new Date(), jobInfo.getRepeatTime(),
                        SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT, jobInfo.getPriorite(),listParam);
            }
            schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobInfo.getJobName()), newTrigger);
            jobInfo.setDisplayName(schedulerFactoryBean.getScheduler().getSchedulerName());
            //ptiJobDetailsRepository.save(jobInfo);  
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    public boolean unScheduleJob(String jobName) {
        try {
            return schedulerFactoryBean.getScheduler().unscheduleJob(new TriggerKey(jobName));
        } catch (SchedulerException e) {
            log.error("Failed to un-schedule job - {}", jobName, e);
            return false;
        }
    }

    public boolean deleteJob(String jobName, String jobGroup) {
        try {
            boolean deleteJob = schedulerFactoryBean.getScheduler().deleteJob(new JobKey(jobName, jobGroup));
            if (deleteJob) {
                //   SchedulerJob job = schedulerRepository.findSchedulerJobByJobName(jobName);  
                // schedulerRepository.delete(job);  
            }
            return deleteJob;
        } catch (SchedulerException e) {
            log.error("Failed to delete job - {}", jobName, e);
            return false;
        }
    }

    public boolean pauseJob(String jobName, String jobGroup) {
        try {
            schedulerFactoryBean.getScheduler().pauseJob(new JobKey(jobName, jobGroup));
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to pause job - {}", jobName, e);
            return false;
        }
    }

    public boolean resumeJob(String jobName, String jobGroup) {
        try {
            schedulerFactoryBean.getScheduler().resumeJob(new JobKey(jobName, jobGroup));
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to resume job - {}", jobName, e);
            return false;
        }
    }

    public boolean startJobNow(String jobName, String jobGroup) {
        try {
            schedulerFactoryBean.getScheduler().triggerJob(new JobKey(jobName, jobGroup));
            log.debug("Start a new job - {}", jobName);
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to start new job - {}", jobName, e);
            return false;
        }
    }

    public void stopSchedulers() throws SchedulerException {
        schedulerFactoryBean.destroy();
    }

    public void startAllSchedulers() throws SchedulerException {
        List<PtiJobDetails> jobInfoList = ptiJobDetailsRepository.findAllActif();
        if (jobInfoList != null) {

            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            jobInfoList.forEach((PtiJobDetails jobInfo) -> {
                try {

                    
                
                    
                    
                    
                    JobDetail jobDetail = JobBuilder.newJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass())).usingJobData("jobName", jobInfo.getJobName())
                            .withIdentity(jobInfo.getJobName(), jobInfo.getNomGroupe()).build();
                     List<ParamExecutionJob> listParam = paramExecutionJobService.findByPtiJobDetails(jobInfo);
            if (listParam.size() > 0) {
                for (ParamExecutionJob lp : listParam) {
                    jobDetail.getJobDataMap().put(lp.getCle(), lp.getValeur());
                }

            }   
                    if (!scheduler.checkExists(jobDetail.getKey())) {
                        Trigger trigger;
                        jobDetail = scheduleCreator.createJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()),
                                false, context, jobInfo.getJobName(), jobInfo.getNomGroupe(),listParam);

                        if (jobInfo.getCronJob() && CronExpression.isValidExpression(jobInfo.getExpressionCron())) {
                            trigger = scheduleCreator.createCronTrigger(jobInfo.getJobName(), new Date(),
                                    jobInfo.getExpressionCron(), CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING, jobInfo.getPriorite(),listParam);
                        } else {
                            trigger = scheduleCreator.createSimpleTrigger(jobInfo.getJobName(), new Date(),
                                    jobInfo.getRepeatTime(), SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT, jobInfo.getPriorite(),listParam);
                        }

                        scheduler.scheduleJob(jobDetail, trigger);

                    }
                } catch (ClassNotFoundException e) {
                    log.error("Class Not Found - {}", jobInfo.getJobClass(), e);
                } catch (SchedulerException e) {
                    log.error(e.getMessage(), e);
                }
            });

            scheduler.getListenerManager().addJobListener(myJobListener, allJobs());
        }
    }
}
