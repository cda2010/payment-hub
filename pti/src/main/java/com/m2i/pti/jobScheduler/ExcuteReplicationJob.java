/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.jobScheduler;

import com.m2i.pti.domain.BackupData;
import com.m2i.pti.domain.BackupDataFields;
import com.m2i.pti.domain.ParamBackUpData;
import com.m2i.pti.repository.BackupDataFieldsRepository;
import com.m2i.pti.repository.BackupDataRepository;
import com.m2i.pti.repository.ParamBackUpDataRepository;
import com.m2i.pti.service.pentaho.ExecuteReplication;
import com.m2i.pti.service.pentaho.ReplicationDomaine;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.pentaho.di.core.Result;

/**
 *
 * @author cda
 */
public class ExcuteReplicationJob implements Serializable, Job {

    @Autowired
    ExecuteReplication executeReplication;
    @Autowired
    ParamBackUpDataRepository paramBackUpDataRepository;
    @Autowired
    BackupDataRepository backUpDataRepository;
    @Autowired
    BackupDataFieldsRepository backupDataFieldsRepository;

    public void executeJob() {

    }

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        ParamBackUpData paramBackUpData = paramBackUpDataRepository.findEnCours();
        DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        String dateTimeRequest;
        String where;
        String whereUpate = "";
        Instant dateRequestnow = Instant.now();
        Instant dateRequest = dateRequestnow.minus(0, ChronoUnit.DAYS);
        String dateTimeRequestnow = formatDate.format(dateRequest);
        List<BackupData> listBackupData = backUpDataRepository.findAll();
        String lastBackupDay = "";
        try {
            KettleEnvironment.init();

            int i = 0;
            for (BackupData replDom : listBackupData) {
                TransMeta transMeta = null;
                TransMeta transMetaUpdate = null;
                transMeta = executeReplication.createTrans();
                transMetaUpdate = transMeta;
                if (replDom.getLastBackup() != null) {
                    Instant instant = replDom.getLastBackup();
                    lastBackupDay = formatDate.format(instant);
                    where = "created_date >='" + lastBackupDay + "' and created_date <='" + dateTimeRequestnow + "'";
                    if (replDom.isUpdatable()) {
                        BackupDataFields backupDataFields = backupDataFieldsRepository.findById(replDom.getId()).orElse(null);
                        whereUpate = "last_modified_date >='" + lastBackupDay + "' and last_modified_date <='" + dateTimeRequestnow + "'";

                    }
                } else {
                    //     paramBackUpData = new ParamBackUpData();
                    dateTimeRequest = formatDate.format(dateRequest);
                    where = "created_date <='" + dateTimeRequestnow + "'";
                }

                Result res;
                i = i + 1;
                replDom.setLastBackup(dateRequestnow);
                if (replDom.isUpdatable()) {

                    transMetaUpdate = executeReplication.generateUpdateTrans(replDom, i, transMetaUpdate, whereUpate);
                    res = executeReplication.runTrans(transMetaUpdate);

                } else {
                    transMeta = executeReplication.generateTrans(replDom, i, transMeta, where);
                    res = executeReplication.runTrans(transMeta);
                }
                if (res.getNrLinesInput() > 0 && res.getResult()) {
                    if (res.getNrErrors() <= 0) {
                              System.out.println(res.getLogText());
                        if (res.getNrLinesWritten() > 0 || res.getNrLinesUpdated() > 0) {
                            backUpDataRepository.save(replDom);
                        }
                    }

                    /*  if (replDom.isUpdatable()) {
                        executeReplication.runTrans(transMetaUpdate);
                    }*/
                }

                //    Result res = executeReplication.runTrans(transMeta);
            }

            paramBackUpDataRepository.save(paramBackUpData);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {

        }

    }

}
