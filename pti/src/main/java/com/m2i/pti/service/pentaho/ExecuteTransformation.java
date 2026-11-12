/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.pentaho;

import com.m2i.pti.repository.FileExportRepository;
import com.m2i.pti.repository.ExportTableRepository;
import java.net.URI;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.pentaho.di.trans.steps.textfileoutput.*;
import com.m2i.pti.domain.ParametreExport;
import com.m2i.pti.domain.ExportTable;
import com.m2i.pti.domain.FileExport;

import com.m2i.pti.domain.FileExportParam;
import com.m2i.pti.domain.enumeration.TypeChamps;
import com.m2i.pti.repository.ParametreExportRepository;

import com.m2i.pti.repository.FileExportParamRepository;
import com.m2i.pti.service.FileExportService;

import java.util.List;
import java.time.format.DateTimeFormatter;


/**
 *
 * @author cda
 */
@Service
public class ExecuteTransformation {

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

    public ExecuteTransformation(FileExportParamRepository fileExportParamRepository, FileExportRepository fileExportRepository, ExportTableRepository exportTableRepository, ParametreExportRepository parametreExportRepository, FileExportService fileExportService) {
        this.exportTableRepository = exportTableRepository;
        this.fileExportRepository = fileExportRepository;
        this.fileExportParamRepository = fileExportParamRepository;
        this.fileExportService = fileExportService;
    }

    private TransMeta createTrans() {
        String cleanURI = replicaConnexionUrl.substring(5);
        URI uri = URI.create(cleanURI);
        TransMeta transMeta = new TransMeta();
        // définir le nom de la transformation       
        transMeta.setName("ChargementTransName");
        // ajouter la connexion à la base de données pour la         transformation 
        transMeta.addDatabase(new DatabaseMeta("fromDbName", uri.getScheme(), "Native(JDBC)", uri.getHost(), uri.getPath().substring(1), uri.getPort() + "", replicaUsername, replicaPassword));
    //    transMeta.addDatabase(new DatabaseMeta("toDbName", "PostgreSQL", "Native(JDBC)", "127.0.0.1", "corisbf4?useSSL=false", "5434", "postgres", "admin"));
        return transMeta;
    }

    private  StepMeta createSelectTableStep(DatabaseMeta db, TransMeta transMetaFile, List<FileExportParam>  fileExportParam) {
        // Créer une nouvelle étape d'entrée de table 
        StepMeta stepTableInput = transMetaFile.findStep("EXTRACTION_DEPUIS_TABLE");
        TableInputMeta tableInputMeta = (TableInputMeta) stepTableInput.getStepMetaInterface();;       // définir la connexion à la base de données à l'étape 1      
        tableInputMeta.setDatabaseMeta(db);
        StepMeta step1 = new StepMeta("SELECT-LIGNE", tableInputMeta);
        // définir le sql à l'étape 1       
        String sql = tableInputMeta.getSQL();         // définit le nom de l'étape     
     String  whereClause=getSqlParametre( fileExportParam, sql);
        tableInputMeta.setSQL(sql + " " + whereClause);
        return step1;

    }

    private static StepMeta createLoadInfile(String filename, TransMeta transMetaFile) {
        // Créer une nouvelle étape d'entrée de table 
        StepMeta stepFileOutput = transMetaFile.findStep("ALIMENTATION_FICHIER");
        TextFileOutputMeta textFileOutputMeta = (TextFileOutputMeta) stepFileOutput.getStepMetaInterface();
        textFileOutputMeta.setFilename(filename);
        stepFileOutput.setStepMetaInterface(textFileOutputMeta);
        // textFileOutputMeta.setDefault();
        //textFileOutputMeta.setFilename(filename);
        //    textFileOutputMeta.setFileFormat("txt");
        //    String outPutStep = "ALIMENTATION_FICHIER";
        // String outputId = PluginRegistry.getInstance().getPluginId(textFileOutputMeta);
        //  StepMeta stepMeta2 = new StepMeta(outputId, outPutStep, textFileOutputMeta);
        //      stepMeta2.setStepMetaInterface(stepFileOutput.getStepMetaInterface());

        return stepFileOutput;

    }

    private static TransHopMeta createHop(StepMeta step1, StepMeta step2) {
        // Définir l'étape de début et l'étape cible, et associer les deux étapes   
        TransHopMeta transHopMeta = new TransHopMeta(step1, step2);
        return transHopMeta;

    }

    private String getValue(FileExportParam fp) {
        if (fp.getParametreExport().getTypeChamps().equals(TypeChamps.NUMBER)) {
            return fp.getValeur();

        } else if (fp.getParametreExport().getTypeChamps().equals(TypeChamps.BOOLEAN)) {
            if (fp.getValeur().equals("VRAI")) {
                return "TRUE";
            } else {
                return "FALSE";
            }

        } else {
            return "'" + fp.getValeur() + "'";
        }
    }

    private String getSqlParametre(List<FileExportParam> fileExportParam,String sql) {
        String whereclause = "";
        int cpt = 1;

        for (FileExportParam fp : fileExportParam) {
            String operateur = "=";

            switch (fp.getOperateur().name()) {
                case "EGALE":
                    // code block
                    operateur = "=";
                    break;
                case "IN":
                    operateur = "IN";
                    break;

                case "INFERIEUR":
                    operateur = "<";
                    break;
                case "SUPERIEUR":
                    operateur = ">";
                    break;
                case "NOT_IN":
                    operateur = "NOT IN";
                    break;
                case "INFERIEUR_EGALE":
                    operateur = "<=";
                    break;
                case "SUPERIEUR_EGALE":
                    operateur = ">=";
                    break;

            }
            String where = fp.getParametreExport().getNomParametre() + operateur + getValue(fp);
            if (cpt == 1) {
                if (sql.indexOf("where")>0 ||sql.indexOf("WHERE")>0){
                    whereclause = " and  " + where;
                }else{
                      whereclause = "where " + where;
                }
              
            } else {
                whereclause = whereclause + " " + fp.getOperateurLogique().name() + " " + where;
            }
            cpt = cpt + 1;

        }
        return whereclause;
    }

    public String getFile(Long fileExportId) {
        FileExport fileExport = fileExportRepository.findById(fileExportId).orElse(null);
        String fichier = directory + "/" + tenant + "/ouput/" + fileExport.getFileName();
        return fichier;
    }

    public void execTrans(Long exportId, Long fileExportId) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss");
            /**
             * Initialize the Kettle Enviornment
             */
            // ExportTable expTble = exportTableRepository
            ExportTable expTble = exportTableRepository.findById(exportId).orElse(null);
            String nomFichier = expTble.getOutputFilename() + "-" + java.time.LocalDateTime.now().format(formatter);
            String chemin = directory + "/" + tenant + "/ouput/" + nomFichier;
            FileExport fileExport = fileExportRepository.findById(fileExportId).orElse(null);
            List<FileExportParam> fileExportParam = fileExportParamRepository.findByExpotable(fileExportId);
            String sqlwhere = getSqlParametre(fileExportParam,"");
            KettleEnvironment.init();
            TransMeta metadata = new TransMeta(directory + expTble.getNomFichier());
            TransMeta transMeta = createTrans();

            StepMeta step1 = createSelectTableStep(transMeta.findDatabase("fromDbName"), metadata, fileExportParam);
            transMeta.addStep(step1);
            StepMeta step2 = createLoadInfile(chemin, metadata);
            transMeta.addStep(step2);
            TransHopMeta TransHopMeta1 = createHop(step1, step2);
            transMeta.addTransHop(TransHopMeta1);
            Trans trans = new Trans(transMeta);
            // Execute the transformation00
            trans.execute(null);
            trans.waitUntilFinished();
            fileExport.setFileName(nomFichier);
            fileExport.setParameter(sqlwhere);
            fileExportService.save(fileExport);
            // checking for errors
            if (trans.getErrors() > 0) {
                System.out.println("Error Executing Transformation");
            }
        } catch (KettleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
