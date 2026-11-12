/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.camel;


import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.KettleEnvironment;

import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author cda
 */
@Service
public class ExecuteChargement {

    public ExecuteChargement() {

    }
   
    //@Scheduled(cron="0 0 0,19 * * *")
  /*@Scheduled(fixedRate = 15000)
    public void executeRequeteCarte() throws KettleException, Exception {
        CamelContext context; 
        context=new DefaultCamelContext();
         
      
         
            // context.addRoutes(new Output());
           //  ProducerTemplate template = context.createProducerTemplate();
              try {
                     context.addRoutes(new OutputFile( ));
                     context.start();
          //   template.sendBody("direct:transaction", "");
              }catch (Exception ex) {
             
            
        }finally{
            Thread.sleep(10); // pour avoir le temps d’exécuter la route
            context.stop();
        }
            
    }
    */
    
}
