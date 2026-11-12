/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.m2i.pti.config.AbstractMetricManager;
/**
 *
 * @author cda
 */
@Service
public class MetricManager extends AbstractMetricManager{
    
    public void incrementServiceCall(String tag, String partnerName,  String apiName, String methodName, String result) {
        Optional.ofNullable(getCounters().get(getCounterUniqueName(tag, partnerName, apiName, methodName, result)))
                .orElse(registerCounter(tag, "ServiceName", partnerName, "apiName", apiName, "methodName", methodName, "result", result)).increment();
    }
}
