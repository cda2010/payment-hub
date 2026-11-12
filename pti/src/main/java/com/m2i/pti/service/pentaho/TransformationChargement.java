/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.pentaho;

import java.net.URI;
import java.util.Arrays;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.userdefinedjavaclass.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.pentaho.di.trans.steps.csvinput.*;

import org.pentaho.di.trans.steps.textfileoutput.*;
import org.pentaho.di.trans.steps.textfileoutput.*;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.springframework.scheduling.annotation.Async;

/**
 *
 * @author cda
 */
@Service
public class TransformationChargement {

    @Value("${spring.datasource.url}")
    private String connexionUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    @Value("${server.port}")
    private String port;

    private TransMeta createTrans() {

        String cleanURI = connexionUrl.substring(5);

        URI uri = URI.create(cleanURI);

        TransMeta transMeta = new TransMeta();
        // définir le nom de la transformation       
        transMeta.setName("ChargementTransName");
        // ajouter la connexion à la base de données pour la         transformation 
        transMeta.addDatabase(new DatabaseMeta("fromDbName", uri.getScheme(), "Native(JDBC)", uri.getHost(), uri.getPath().substring(1), uri.getPort() + "", username, password));
        //    transMeta.addDatabase(new DatabaseMeta("toDbName", "PostgreSQL", "Native(JDBC)", "127.0.0.1", "corisbf4?useSSL=false", "5434", "postgres", "admin"));
        transMeta.setUsingThreadPriorityManagment(true);
        transMeta.setFeedbackShown(true);
        transMeta.setFeedbackSize(10000);
        transMeta.setSizeRowset(10000);
        return transMeta;
    }

    public void runTrans(TransMeta transMeta) throws KettleException {
        Trans trans = new Trans(transMeta);

        trans.execute(null);// Exécute la transition       
        trans.waitUntilFinished();
        Result res = trans.getResult();
        System.out.println(res.getRows());
        System.out.println("Nombre de ligne en entrée" + res.getNrLinesInput());
        //     System.out.println("Nombre de ligne en entrée"+res.);
    }

    private StepMeta createStepJava(String nomStep) {
        final String UDJC_METHOD = "import  java.lang.*; import  java.util.*;\n"
                + "import java.math.BigInteger;\n"
                + "import com.pti.emeraude.gateway.PtiSetup;\n"
                + "  import org.springframework.beans.factory.annotation.Autowired;\n "
                + "private BigInteger lastId;\n"
                + "  PtiSetup pti = new PtiSetup();\n"
                + "public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException \n"
                + "{\n"
                + "  // java.util.List timeLine = twitter.getUserTimeline();\n"
                + "\n"
                + "  Object[] r= getRow();\n"
                + "\n"
                + "if (r==null){\n"
                + "\n"
                + "setOutputDone();\n"
                + "return false;\n"
                + "}\n"
                + "Object[] outputRow= createOutputRow(r, data.outputRowMeta.size());\n"
                + "\n"
                + "String idLigne = get(Fields.In, \"idLigne\").getString(r);\n"
                + "\n"
                + "String codeoperation = get(Fields.In, \"codeoperation\").getString(r);\n"
                + "String min = get(Fields.In, \"min\").getString(r);\n"
                + "String max = get(Fields.In, \"max\").getString(r);\n"
                + "\n"
                + "String compteId = get(Fields.In, \"compteId\").getString(r);\n"
                + "\n"
                + "\n"
                + "try{    \n"
                + "\n"
                + "pti.executeChargement( Long.parseLong(idLigne),Long.parseLong(compteId) , Long.parseLong(codeoperation),Long.parseLong(min) , Long.parseLong(max) ) ;\n"
                + "//code that may throw an exception    \n"
                + "}catch(Exception ex){\n"
                + "logBasic(ex.getMessage());\n"
                + "}    \n"
                + "\n"
                + "  return true;\n"
                + "}\n"
                + "\n"
                + "public boolean init(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) {\n"
                + "   if (super.init(stepMetaInterface, stepDataInterface)) {\n"
                + "        pti.setPlainClientCredentials(\"$2a$10$bXAyslP4KRJzwraJCLccpOCWd583.f/U2TGPV6a9WP50FcNNrbaF.\");\n"
                + "        pti.setPassword(\"$2a$10$bXAyslP4KRJzwraJCLccpOCWd583.f/U2TGPV6a9WP50FcNNrbaF.\");\n"
                + "        pti.setClientID(\"ptimobile\");\n"
                + "        pti.setUrl(\"http://localhost:" + port + "\");\n"
                + "        pti.setGrantUri(\"?grant_type=client_credentials&scope=read&client_secret=\" + pti.getPlainClientCredentials() + \"&client_id=\" + pti.getClientID());\n"
                + "           \n"
                + "     return true;\n"
                + "   }\n"
                + "   return false;\n"
                + "}";

        UserDefinedJavaClassMeta udjcMeta = new UserDefinedJavaClassMeta();
        //   udjcMeta.getInfoStepDefinitions().add(new StepDefinition(dg2.getName(), dg2.getName(), dg2, "info_data"));
        udjcMeta.replaceDefinitions(Arrays.asList(new UserDefinedJavaClassDef[]{new UserDefinedJavaClassDef(UserDefinedJavaClassDef.ClassType.TRANSFORM_CLASS, "MainClass", UDJC_METHOD)}));
        StepMeta udjc = new StepMeta(nomStep, udjcMeta);
        return udjc;
    }

    private static StepMeta createSelectStep(TransMeta transMeta, String idChargement, String codeoperation, String compteId) {
        // Créer une nouvelle étape d'entrée de table 

        TableInputMeta tableInputMeta = new TableInputMeta();         // définir la connexion à la base de données à l'étape 1      
        tableInputMeta.setDatabaseMeta(transMeta.findDatabase("fromDbName"));
        // définir le sql à l'étape 1       

        tableInputMeta.setSQL("SELECT id as idLigne, " + codeoperation + " as codeoperation ," + compteId + " as compteId, min, max FROM ligne_chargement_transfert l, (select min(id) as min, max(id) as max  from ligne_chargement_transfert) m where chargement_transfert_id= " + idChargement + "and statut_ligne!='OC' and compte_id is not null order by id");         // définit le nom de l'étape      
        StepMeta step1 = new StepMeta("SELECT-LIGNE-CHARGEMENT", tableInputMeta);

        return step1;

    }

//     private TextFileOutputMeta createFileMeta(File file, TextFileInputField[] fields) {
//        TextFileOutputMeta meta = new TextFileOutputMeta();
//        meta.setFileName(username);
//        meta.setExtension(csv);
//        meta.setFilename(file.getAbsolutePath());
//        meta.setDelimiter(";");
//        meta.setEncoding(encoding);
//        meta.setEnclosure("");
//        meta.setBufferSize("50000");
//        meta.setInputFields(fields);
//        meta.setHeaderPresent(false);
//        TextFileInputField f ;
//        
//        return meta;
//    }
//    
//    private CsvInputMeta createTextMeta(File file, TextFileInputField[] fields) {
//        CsvInputMeta meta = new CsvInputMeta();
//        meta.setFilename(file.getAbsolutePath());
//        meta.setDelimiter(";");
//        meta.setEncoding(encoding);
//        meta.setEnclosure("");
//        meta.setBufferSize("50000");
//        meta.setInputFields(fields);
//        meta.setHeaderPresent(false);
//        TextFileInputField f ;
//        
//        return meta;
//    }
    private static TransHopMeta createHop(StepMeta step1, StepMeta step2) {
        // Définir l'étape de début et l'étape cible, et associer les deux étapes   
        TransHopMeta transHopMeta = new TransHopMeta(step1, step2);
        return transHopMeta;

    }

    public TransMeta generateTrans(String idChargement, String codeoperation, String compteId) throws KettleException {
        TransMeta transMeta = createTrans();
        // Créer l'étape 1 et ajouter à transition StepMeta        
        StepMeta stepSelect = createSelectStep(transMeta, idChargement, codeoperation, compteId);
        stepSelect.setDistributes(true);

        transMeta.addStep(stepSelect);

        // créer l'étape 2 et l'ajouter à la transition  
        StepMeta stepJava1 = createStepJava("JAVA-USERDEFINED0");
     //   StepMeta stepJava2 = createStepJava("JAVA-USERDEFINED1");
        
    //    StepMeta stepJava3 = createStepJava("JAVA-USERDEFINED2");
        
      //  StepMeta stepJava4 = createStepJava("JAVA-USERDEFINED3");
        /*
        StepMeta stepJava5 = createStepJava("JAVA-USERDEFINED4");
        StepMeta stepJava6 = createStepJava("JAVA-USERDEFINED5");
        StepMeta stepJava7 = createStepJava("JAVA-USERDEFINED6");
        StepMeta stepJava8 = createStepJava("JAVA-USERDEFINED7");
        StepMeta stepJava9 = createStepJava("JAVA-USERDEFINED8");
        StepMeta stepJava10 = createStepJava("JAVA-USERDEFINED9");
*/
        transMeta.addStep(stepJava1);
   //     transMeta.addStep(stepJava2);

        
     //   transMeta.addStep(stepJava3);
        
      //  transMeta.addStep(stepJava4);
        /*
        transMeta.addStep(stepJava3);

        transMeta.addStep(stepJava4);
        transMeta.addStep(stepJava5);
        transMeta.addStep(stepJava6);

        transMeta.addStep(stepJava7);
        transMeta.addStep(stepJava8);
        transMeta.addStep(stepJava9);

        transMeta.addStep(stepJava10);
         */
        // créer une connexion de saut et ajouter un saut       
        TransHopMeta TransHopMeta1 = createHop(stepSelect, stepJava1);
  //      TransHopMeta TransHopMeta2 = createHop(stepSelect, stepJava2);

        
    //    TransHopMeta TransHopMeta3 = createHop(stepSelect, stepJava3);
        
  //      TransHopMeta TransHopMeta4 = createHop(stepSelect, stepJava4);
       
        /*TransHopMeta TransHopMeta5 = createHop(stepSelect, stepJava5);
        TransHopMeta TransHopMeta6 = createHop(stepSelect, stepJava6);

        TransHopMeta TransHopMeta7 = createHop(stepSelect, stepJava7);
        TransHopMeta TransHopMeta8 = createHop(stepSelect, stepJava8);
        TransHopMeta TransHopMeta9 = createHop(stepSelect, stepJava9);
        TransHopMeta TransHopMeta10 = createHop(stepSelect, stepJava10);
         */
        transMeta.addTransHop(TransHopMeta1);
       // transMeta.addTransHop(TransHopMeta2); 
       // transMeta.addTransHop(TransHopMeta3);
        
     //   transMeta.addTransHop(TransHopMeta4);
        /*
        transMeta.addTransHop(TransHopMeta5);
        transMeta.addTransHop(TransHopMeta6);

        transMeta.addTransHop(TransHopMeta7);
        transMeta.addTransHop(TransHopMeta8);
        transMeta.addTransHop(TransHopMeta9);

        transMeta.addTransHop(TransHopMeta10);
*/         
        return transMeta;

    }

}
