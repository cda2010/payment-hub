package com.m2i.pti.batch.reader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.activation.FileDataSource;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class JobService {

    @Autowired
    @Qualifier("asyncJobLauncher")
    JobLauncher jobLauncher;

    // @Autowired
    //@Qualifier(value = "insertFileinDB")
    // Job jobCreateTransfertLine;
    @Autowired
    @Qualifier(value = "ligneTransValidationJob")
    Job jobValideTransfertLine;


    public void startValidation(Long chargementId, Long codeoperation, Long compteId) {
        batchValidation(chargementId, codeoperation, compteId);
    }


    public void startFileUpload(MultipartFile uploadFile, Long chargementId, HttpServletResponse response) throws IOException {

        File convFile = new File(uploadFile.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(uploadFile.getBytes());
        fos.close();
        //  batchTrigger(uploadFile.getOriginalFilename(), chargementId);

    }

    /*
    private void batchTrigger(String fileName, Long chargementId) {
        JobParameters jobParameters = new JobParametersBuilder().addString("fileName", fileName)
                .addDate("1", new Date())
                .addLong("chargementId", chargementId)
                .toJobParameters();
        JobExecution job = null;

        try {
          //  job = jobLauncher.run(jobCreateTransfertLine, jobParameters);
          //  System.out.println("JOB ID " + job.getJobInstance().getId());

        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }*/

    private void batchValidation(Long chargementId, Long codeoperation, Long compteId) {
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("date_validation", new Date())
                .addLong("chargementId", chargementId)
                .addLong("codeoperation", codeoperation)
                .addLong("compteId", compteId)
                .toJobParameters();
        JobExecution job = null;

        try {
            job = jobLauncher.run(jobValideTransfertLine, jobParameters);
            System.out.println("JOB ID " + job.getJobInstance().getId());

        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
