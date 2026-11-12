/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.camel;



import com.m2i.pti.domain.LigneChargementTransfert;
import java.io.IOException;
import java.util.List;




/**
 *
 * @author cda
 */
public class OutputFile  {
   // private static CamelContext context;
   
    public OutputFile() {
     
    }
   /*

    @Override
    public void configure() throws Exception {
        /*
        onException(Exception.class,Exception.class)
             
         .handled(true) .maximumRedeliveries(3);
              errorHandler(defaultErrorHandler()

.retryAttemptedLogLevel(LoggingLevel.ERROR));*/
        /*
        from("jpa://"+LigneChargementTransfert.class.getName()+"?nativeQuery=select * from LigneChargementTransfert c where c.chargementTransfert.id = 1")
                 /*
                 process(new Processor() {  
                     public void process(Exchange exchange) throws Exception {   
                         System.out.println("We just downloaded: "            + exchange.getIn().getHeader("CamelFileName"));   
                     } })*/
    
    /*
                .log("DEBUT")
               
                .log("${body}");
    }
    */
}
