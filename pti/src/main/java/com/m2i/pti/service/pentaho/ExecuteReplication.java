/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.pentaho;

import com.m2i.pti.domain.BackupData;
import com.m2i.pti.domain.BackupDataFields;
import com.m2i.pti.repository.FileExportRepository;
import com.m2i.pti.repository.ExportTableRepository;
import java.net.URI;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.m2i.pti.repository.BackupDataFieldsRepository;
import com.m2i.pti.repository.ParametreExportRepository;

import com.m2i.pti.repository.FileExportParamRepository;
import com.m2i.pti.service.FileExportService;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UsageParameter;
import org.pentaho.di.trans.steps.synchronizeaftermerge.SynchronizeAfterMergeMeta;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta;
import java.util.List;
import java.util.HashMap;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.trans.steps.update.UpdateMeta;
import org.pentaho.di.trans.steps.sql.ExecSQLMeta;

/**
 *
 * @author cda
 */
@Service
public class ExecuteReplication {
    
    @Value("${spring.datasource.url}")
    private String connexionUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${server.port}")
    private String port;
    @Value("${pentaho.trans.dir}")
    private String directory;
    @Value("${tenant.numero}")
    private String tenant;
    
     @Value("${replica.datasource.username}")
    private String replicaUsername;
    
    @Value("${replica.datasource.password}")
    private String replicaPassword;
    
    @Value("${replica.datasource.url}")
    private String replicaConnexionUrl;
     
    private ExportTableRepository exportTableRepository;
    
    private ParametreExportRepository parametreExportRepository;
    private FileExportRepository fileExportRepository;
    private FileExportParamRepository fileExportParamRepository;
    private final FileExportService fileExportService;
    private final BackupDataFieldsRepository backupDataFieldsRepository;
    
    public ExecuteReplication(BackupDataFieldsRepository backupDataFieldsRepository, FileExportParamRepository fileExportParamRepository, FileExportRepository fileExportRepository, ExportTableRepository exportTableRepository, ParametreExportRepository parametreExportRepository, FileExportService fileExportService) {
        this.exportTableRepository = exportTableRepository;
        this.fileExportRepository = fileExportRepository;
        this.fileExportParamRepository = fileExportParamRepository;
        this.fileExportService = fileExportService;
        this.backupDataFieldsRepository = backupDataFieldsRepository;
        
    }
    
    public TransMeta createTrans() {
        String cleanURI = connexionUrl.substring(5);
       String cleanURIDest = replicaConnexionUrl.substring(5);
        URI uri = URI.create(cleanURI);
       URI uriDest = URI.create(cleanURIDest);
        TransMeta transMeta = new TransMeta();
        // définir le nom de la transformation       
        transMeta.setName("REPLICATON");
        String b = uri.getPath().substring(1);
        DatabaseMeta d = new DatabaseMeta();

        // ajouter la connexion à la base de données pour la         transformation 
        transMeta.addDatabase(new DatabaseMeta("fromDbNameDest", uri.getScheme(), "Native(JDBC)", uriDest.getHost(), uriDest.getPath().substring(1), uriDest.getPort() + "", replicaUsername, replicaPassword));
        transMeta.addDatabase(new DatabaseMeta("fromDbName", uri.getScheme(), "Native(JDBC)", uri.getHost(), uri.getPath().substring(1), uri.getPort() + "", username, password));
        transMeta.setUsingThreadPriorityManagment(true);
        transMeta.setFeedbackShown(true);
        transMeta.setFeedbackSize(10000);
        transMeta.setSizeRowset(10000);
    transMeta.addDatabase(new DatabaseMeta("toDbName", "PostgreSQL", "Native(JDBC)", "127.0.0.1", "bdbf4?useSSL=false", "5432", "postgres", "admin"));
        return transMeta;
    }
    
    private static StepMeta createSelectStepTable(TransMeta transMeta, String table, int idx, String where) {
        // Créer une nouvelle étape d'entrée de table 
        TableInputMeta tableInputMeta = new TableInputMeta();
        // définir la connexion à la base de données à l'étape 1      
        tableInputMeta.setDatabaseMeta(transMeta.findDatabase("fromDbName"));
        // définir le sql à l'étape 1  
        String sql = "SELECT * from " + table + " where " + where;
        System.out.println(sql);
        tableInputMeta.setSQL(sql);         // définit le nom de l'étape      
        StepMeta step1 = new StepMeta("SELECT-LIGNE-" + table + idx, tableInputMeta);
        return step1;
        
    }
    
    private HashMap<String, Object[]> getBackupfieldAndStream(BackupData replDom) {
        HashMap<String, Object[]> backUpStreamMap = new HashMap<>();
        List<BackupDataFields> listeDataFields = backupDataFieldsRepository.findByBackupData(replDom.getId());
        if (listeDataFields != null && listeDataFields.size() > 0) {
            String[] keyLookup = new String[listeDataFields.size()];
            String[] keyStream = new String[listeDataFields.size()];
             Boolean[]keyBool = new Boolean[listeDataFields.size()];
            int i = 0;
            for (BackupDataFields backupDataFields : listeDataFields) {
                keyLookup[i] = backupDataFields.getFieldToUpdate();
                keyStream[i] = backupDataFields.getStreamForUpdate();
                keyBool[i]=Boolean.TRUE;
            }
            backUpStreamMap.put("keyLookup", keyLookup);
            backUpStreamMap.put("keyStream", keyStream);
            backUpStreamMap.put("keyBool", keyBool);
        }
        return backUpStreamMap;
    }
    
    private StepMeta createSelectForUpdateStepTable(TransMeta transMeta, String table, int idx, String where, BackupData replDom) {
        // Créer une nouvelle étape d'entrée de table 
        TableInputMeta tableInputMeta = new TableInputMeta();
        // définir la connexion à la base de données à l'étape 1      
        tableInputMeta.setDatabaseMeta(transMeta.findDatabase("fromDbName"));
        String champSelect = "";
        // définir le sql à l'étape 1
        List<BackupDataFields> listeDataFields = backupDataFieldsRepository.findByBackupData(replDom.getId());
        if (listeDataFields != null && listeDataFields.size() > 0) {
            int i = 0;
            for (BackupDataFields backupDataFields : listeDataFields) {
                if (i == 0) {
                    champSelect = backupDataFields.getStreamForUpdate();
                } else {
                    champSelect = champSelect + "," + backupDataFields.getStreamForUpdate();
                }
            }
        }
        String sql = "SELECT   " + replDom.getTableSourceKey() + "," + champSelect + ", 'UPDATE' as fieldFlag  from " + table + " where " + where;
        System.out.println(sql);
        tableInputMeta.setSQL(sql);         // définit le nom de l'étape      
        
        StepMeta step1 = new StepMeta("SELECT-LIGNE-" + table + idx, tableInputMeta);
        return step1;
        
    }
    
    private StepMeta createInsertStepTable(TransMeta transMeta, String nomTable, int idx) {
        // Créer une nouvelle étape d'entrée de table 
        TableOutputMeta tableOutputMeta = new TableOutputMeta();
        tableOutputMeta.setDatabaseMeta(transMeta.findDatabase("fromDbNameDest"));
        tableOutputMeta.setTableName(nomTable);
        
        StepMeta step1 = new StepMeta("INSERT-LIGNE" + idx, tableOutputMeta);
        // définir le sql à l'étape 1       

        return step1;
        
    }
    
    private StepMeta createUpdateScriptStepTable(TransMeta transMeta, String table, BackupData replDom) {
        // Créer une nouvelle étape d'entrée de table 
        ExecSQLMeta esSqlMeta = new ExecSQLMeta();
        // définir la connexion à la base de données à l'étape 1      
        esSqlMeta.setDatabaseMeta(transMeta.findDatabase("fromDbName"));
        String champUpdate = "";
        List<BackupDataFields> listeDataFields = backupDataFieldsRepository.findByBackupData(replDom.getId());
        
        String where = replDom.getTableDestKey() + "";
        if (listeDataFields != null && listeDataFields.size() > 0) {
            int i = 0;
            for (BackupDataFields backupDataFields : listeDataFields) {
                if (i == 0) {
                    champUpdate = backupDataFields.getFieldToUpdate() + "=" + backupDataFields.getStreamForUpdate();
                } else {
                    champUpdate = champUpdate + "," + backupDataFields.getFieldToUpdate() + "=" + backupDataFields.getStreamForUpdate();
                }
            }
        }
        
        esSqlMeta.setSql("update " + table + " set " + champUpdate + " where " + where + ";");
        // définit le nom de l'étape      
        StepMeta step1 = new StepMeta("DEL-TRANSACTION", esSqlMeta);
        return step1;
        
    }
    
    private StepMeta test(BackupData replDom, TransMeta transMeta, String nomTable, int idx) {
        SynchronizeAfterMergeMeta updateMeta = new SynchronizeAfterMergeMeta();
        updateMeta.setDatabaseMeta(transMeta.findDatabase("fromDbNameDest"));
        updateMeta.setTableName(nomTable);
        updateMeta.setCommitSize(100);
        updateMeta.setUseBatchUpdate(true);
        String[] keyLookup = new String[1];
        String[] keyCondition = new String[1];
        String[] keyStream = new String[1];
        keyCondition[0] = "=";
        keyLookup[0] = replDom.getTableDestKey();
        keyStream[0] = replDom.getTableSourceKey();
        updateMeta.setKeyLookup(keyLookup);
        updateMeta.setKeyCondition(keyCondition);
        updateMeta.setKeyStream(keyStream);
        updateMeta.setOperationOrderField("fieldFlag");
        updateMeta.setOrderUpdate("UPDATE");
        HashMap<String, Object[]> backUpStreamMap = getBackupfieldAndStream(replDom);
        if (!backUpStreamMap.isEmpty()) {
            updateMeta.setUpdateLookup((String[])backUpStreamMap.get("keyLookup"));
            updateMeta.setUpdateStream((String[])backUpStreamMap.get("keyStream"));
            updateMeta.setUpdate((Boolean[])backUpStreamMap.get("keyBool"));
        }
     
        StepMeta step1 = new StepMeta("UPDATE-LIGNE" + idx, updateMeta);
        return step1;
    }
    
    private StepMeta createUpdateStepTable(BackupData replDom, TransMeta transMeta, String nomTable, int idx) {
        // Créer une nouvelle étape d'entrée de table 
        UpdateMeta updateMeta = new UpdateMeta();
        
        updateMeta.setDatabaseMeta(transMeta.findDatabase("fromDbNameDest"));
        updateMeta.setTableName(nomTable);
        updateMeta.setCommitSize("100");
        updateMeta.setUseBatchUpdate(true);
        String[] keyLookup = new String[1];
        String[] keyCondition = new String[1];
        String[] keyStream = new String[1];
        keyCondition[0] = "=";
        keyLookup[0] = replDom.getTableDestKey();
        keyStream[0] = replDom.getTableSourceKey();
        updateMeta.setKeyLookup(keyLookup);
        updateMeta.setKeyCondition(keyCondition);
        updateMeta.setKeyStream(keyStream);
        
        HashMap<String, Object[]> backUpStreamMap = getBackupfieldAndStream(replDom);
        if (!backUpStreamMap.isEmpty()) {
            updateMeta.setUpdateLookup((String[])backUpStreamMap.get("keyLookup"));
            updateMeta.setUpdateStream((String[])backUpStreamMap.get("keyStream"));
            
        }
        
        StepMeta step1 = new StepMeta("UPDATE-LIGNE" + idx, updateMeta);
        // définir le sql à l'étape 1       

        return step1;
        
    }
    
    public Result runTrans(TransMeta transMeta) throws KettleException {
        Trans trans = new Trans(transMeta);
        trans.setLogLevel(LogLevel.ERROR);
        trans.execute(null);// Exécute la transition       
        trans.waitUntilFinished();
        Result res = trans.getResult();
        System.out.println(res.getRows());
        System.out.println("Nombre de ligne en entrée" + res.getResult());
        return res;
        //     System.out.println("Nombre de ligne en entrée"+res.);
    }
    
    private static StepMeta createDeleScriptStepTable(TransMeta transMeta, String table, String where) {
        // Créer une nouvelle étape d'entrée de table 
        ExecSQLMeta esSqlMeta = new ExecSQLMeta();
        // définir la connexion à la base de données à l'étape 1      
        esSqlMeta.setDatabaseMeta(transMeta.findDatabase("fromDbName"));
        esSqlMeta.setSql("delete from " + table + " where " + where + ";");
        // définit le nom de l'étape      
        StepMeta step1 = new StepMeta("DEL-TRANSACTION", esSqlMeta);
        return step1;
        
    }
    
    private static TransHopMeta createHop(StepMeta step1, StepMeta step2) {
        // Définir l'étape de début et l'étape cible, et associer les deux étapes   
        TransHopMeta transHopMeta = new TransHopMeta(step1, step2);
        return transHopMeta;
        
    }
    
    public TransMeta generateUpdateTrans(BackupData replDom, int idx, TransMeta transMeta, String whereClause) throws KettleException {
        //  TransMeta transMeta = createTrans();
        StepMeta stepSelect = createSelectForUpdateStepTable(transMeta, replDom.getTableSource(), idx, whereClause, replDom);
        stepSelect.setDistributes(true);
        transMeta.addStep(stepSelect);
        StepMeta stepInsert = test(replDom, transMeta, replDom.getTableDest(), idx);
        transMeta.addStep(stepInsert);
        TransHopMeta transHopMeta = createHop(stepSelect, stepInsert);
        transMeta.addTransHop(transHopMeta);
        return transMeta;
        
    }
    
    public TransMeta generateTrans(BackupData replDom, int idx, TransMeta transMeta, String whereClause) throws KettleException {
        //  TransMeta transMeta = createTrans();
        StepMeta stepSelect = createSelectStepTable(transMeta, replDom.getTableSource(), idx, whereClause);
        stepSelect.setDistributes(false);
        transMeta.addStep(stepSelect);
        StepMeta stepInsert = createInsertStepTable(transMeta, replDom.getTableDest(), idx);
        transMeta.addStep(stepInsert);
        TransHopMeta transHopMeta = createHop(stepSelect, stepInsert);
        transMeta.addTransHop(transHopMeta);
        if (replDom.getDeleteAfter() == 1) { //**Suppression après insertion
            StepMeta stepSql = createDeleScriptStepTable(transMeta, replDom.getTableSource(), whereClause);
            System.out.println(transMeta.getSteps().size());
            transMeta.addStep(stepSql);
            TransHopMeta transHopMeta2 = createHop(stepInsert, stepSql);
            transMeta.addTransHop(transHopMeta2);
            
        }
        
        return transMeta;
        
    }
}
