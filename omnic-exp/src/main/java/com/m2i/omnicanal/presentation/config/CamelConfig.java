/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.config;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author cda
 */
//@Configuration
public class CamelConfig {

    @Autowired
    private CamelContext camelContext;

    @Bean
    ProducerTemplate producerTemplate() {
        return camelContext.createProducerTemplate();
    }

    @Bean
    ConsumerTemplate consumerTemplate() {
        return camelContext.createConsumerTemplate();
    }
}
