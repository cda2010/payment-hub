/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.easyBatch;

import org.easybatch.core.job.Job;
import static org.easybatch.core.job.JobBuilder.aNewJob;
import org.easybatch.core.job.JobReport;
import org.springframework.stereotype.Service;
import org.easybatch.core.job.JobExecutor;
import org.easybatch.core.processor.RecordProcessor;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.easybatch.core.filter.RecordFilter;
import org.easybatch.core.job.DefaultJobReportMerger;
import org.easybatch.core.job.JobBuilder;
import org.easybatch.core.job.JobReportMerger;
import org.easybatch.core.listener.BatchListener;
import org.easybatch.core.listener.JobListener;
import org.easybatch.core.reader.IterableRecordReader;

/**
 *
 * @author cda
 */
@Service
public class Read {

    //private JdbcTemplate jdbcTemplate;
    private static final int THREAD_POOL_SIZE = 5;

    public Read() {

    }

    public <T extends Object> T getBatch(List<T> ligne, final RecordProcessor recordProcessor) {
        T result = null;
        Job job1;

        job1 = (Job) buildJob(ligne, new RecordFilterCustom(1), "worker-job1", recordProcessor);

        JobExecutor jobExecutor = new JobExecutor(THREAD_POOL_SIZE);
        JobReport report = jobExecutor.execute(job1);
        System.out.println(report);
        jobExecutor.shutdown();
        return result;
    }

    public <T extends Object> T getBatchParal(List<T> ligne, final RecordProcessor recordProcessor, final RecordFilter recordFilter, final RecordFilter recordFilter2) throws InterruptedException, ExecutionException {
        T result = null;
        Job job1;
        Job job2;
        job1 = (Job) buildJobParal(ligne, recordFilter, "worker-job1", recordProcessor);
        job2 = (Job) buildJobParal(ligne, recordFilter2, "worker-job2", recordProcessor);
        JobExecutor jobExecutor = new JobExecutor(THREAD_POOL_SIZE);
       
         List<Future<JobReport>> partialReports = jobExecutor.submitAll(job1, job2);
        //merge partial reports into a global one
        JobReport report1 = partialReports.get(0).get();
        JobReport report2 = partialReports.get(1).get();

        JobReportMerger reportMerger = new DefaultJobReportMerger();
        JobReport finalReport = reportMerger.mergerReports(report1, report2);
        System.out.println(finalReport);

        jobExecutor.shutdown();

        
        
        JobReport report = jobExecutor.execute(job1);
        System.out.println(report);
        jobExecutor.shutdown();
        return result;
    }

    public <T extends Object> T getBatch2(List<T> ligne, final RecordProcessor recordProcessor) {
        Job job = aNewJob()
                .reader(new IterableRecordReader(ligne))
                .filter(new RecordFilterCustom(0))
                // .reader(new JdbcRecordReader(jdbcTemplate.getDataSource(), query))
                // .mapper(new JdbcRecordMapper<>(resultClass))
                .processor(recordProcessor)
                .build();

        // Execute the job
        JobExecutor jobExecutor = new JobExecutor();
        JobReport jobReport = jobExecutor.execute(job);
        JobReport report = jobExecutor.execute(job);

        System.out.println(report);

        jobExecutor.shutdown();

        return (T) job;
    }

    private <T extends Object> T buildJob(List<T> ligne, RecordFilter recordFilter, String jobName, RecordProcessor recordProcessor) {
        return (T) JobBuilder.aNewJob()
                .named(jobName)
                .reader(new IterableRecordReader(ligne))
              ///  .filter(recordFilter)
                .processor(recordProcessor)
                
                .batchSize(1000)
                .build();
    }
 private <T extends Object> T buildJobParal(List<T> ligne, RecordFilter recordFilter, String jobName, RecordProcessor recordProcessor) {
        return (T) JobBuilder.aNewJob()
                .named(jobName)
                .reader(new IterableRecordReader(ligne))
                .filter(recordFilter)
                .processor(recordProcessor)
              
                .batchSize(1000)
                .build();
    }
}
