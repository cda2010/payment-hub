/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 *
 * @author cda
 */
@Component
public class LogMessage extends RouteBuilder {

    private static CamelContext context;

    @Override
    public void configure() throws Exception {
        from("direct:logOmnicanal")
                .to("file:c:/m2i/out?fileExist=Append&fileName=logOmnicanal-${date:now:yyyyMMdd}.trc")
                .to("seda:processOrder");
        from("seda:processOrder") // receive from the seda queue
                .log("Processing an order...")
                .to("file:orders/out");
    }

}
